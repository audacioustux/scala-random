import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import concurrent.duration.DurationInt
import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class Product(name: String)
case class Purchase(products: Map[Product, Int])
case class Client(name: String, address: Point)
case class Point(x: Int, y: Int)
case class StockHouse(products: Map[Product, Int], address: Point):
  def removeProduct(product: Product, quantity: Int): StockHouse =
    copy(products = products.updated(product, products(product) - quantity))

object ClientService {
  sealed trait Message
  case object OrderShipped extends Message
  case object OrderDelayed extends Message

  def apply(client: Client)(using
      processing_svc: ActorRef[ProcessingService.Message]
  ): Behavior[Message] =
    // buys random number of random products
    val products = (1 to 5).map { _ =>
      Product(s"item-${scala.util.Random.nextInt(4) + 1}") -> (scala.util.Random
        .nextInt(4) + 1)
    }.toMap

    apply(client, Purchase(products))

  def apply(
      client: Client,
      purchase: Purchase
  )(using
      processing_svc: ActorRef[ProcessingService.Message]
  ): Behavior[Message] =
    Behaviors.setup { context =>
      context.log.info(s"${context.self.path.name} buying $purchase")
      // send purchase to processing service
      processing_svc ! ProcessingService.PurchaseConfirmed(
        purchase,
        client,
        context.self
      )

      Behaviors.receiveMessage {
        case OrderShipped =>
          context.log.info(s"${context.self.path.name} order shipped")
          Behaviors.stopped
        case OrderDelayed =>
          context.log.info(s"${context.self.path.name} order delayed")
          Behaviors.stopped
      }
    }
}

object ProcessingService {
  sealed trait Message
  case class PurchaseConfirmed(
      purchase: Purchase,
      client: Client,
      replyTo: ActorRef[ClientService.Message]
  ) extends Message
  case class InStock(
      products: Map[Product, Int],
      stockHouse: ActorRef[StockHouseService.Message]
  ) extends Message
  case object OrderDelayed extends Message

  def purchaseProcessor(
      purchase: Purchase,
      stockHouses: List[ActorRef[StockHouseService.Message]],
      client: ActorRef[ClientService.Message]
  ): Behavior[Message] =
    Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        // OrderDelayed message is sent if order is not shipped in 3 seconds
        timers.startSingleTimer("orderDelayed", OrderDelayed, 3.seconds)
        (purchase.products.isEmpty, stockHouses.isEmpty) match {
          case (true, _) =>
            // all products are in stock
            client ! ClientService.OrderShipped
            Behaviors.stopped
          case (_, true) =>
            // no stock houses left, and not all products are in stock
            context.log.info(
              s"${client.path.name}'s order delayed, ${purchase.products} not in stock"
            )
            client ! ClientService.OrderDelayed
            Behaviors.stopped
          case _ =>
            // wait for InStock message from stock house
            Behaviors.receiveMessagePartial {
              case InStock(products, stockHouse) =>
                context.log.info(
                  s"${client.path.name}'s purchase $products in stock at ${stockHouse.path.name}"
                )
                purchaseProcessor(
                  purchase.copy(purchase.products -- products.keySet),
                  stockHouses.filterNot(_ == stockHouse),
                  client
                )
              case OrderDelayed =>
                context.log.info(
                  s"${client.path.name}'s order delayed, stock houses did not respond"
                )
                client ! ClientService.OrderDelayed
                Behaviors.stopped
            }
        }
      }
    }

  def apply(
      stockHouseServices: List[ActorRef[StockHouseService.Message]]
  ): Behavior[Message] = {
    Behaviors.setup { context =>
      import akka.actor.typed.scaladsl.AskPattern._
      val stockHousesByAddress = stockHouseServices.map { stockHouse =>
        given timeout: akka.util.Timeout = 3.seconds
        given scheduler: akka.actor.typed.Scheduler = context.system.scheduler

        val address = Await.result[Point](
          stockHouse.ask(StockHouseService.GetLocation(_)),
          timeout.duration
        )

        address -> stockHouse
      }.toMap

      Behaviors.receiveMessagePartial {
        case PurchaseConfirmed(purchase, client, replyTo) =>
          val stockHouses = stockHousesByAddress.toList
            .sortBy { case (address, _) =>
              math.pow(address.x - client.address.x, 2) + math.pow(
                address.y - client.address.y,
                2
              )
            }
            .take(5)
            .map(_._2)

          val orderProcessorRef = context.spawnAnonymous(
            purchaseProcessor(purchase, stockHouses, replyTo)
          )

          stockHouses.foreach { stockHouse =>
            stockHouse ! StockHouseService.FillOrder(
              purchase.products,
              orderProcessorRef
            )
          }

          Behaviors.same
      }
    }
  }
}

object StockHouseService {
  sealed trait Message
  case class GetLocation(replyTo: ActorRef[Point]) extends Message
  case class FillOrder(
      products: Map[Product, Int],
      replyTo: ActorRef[ProcessingService.Message]
  ) extends Message

  def apply(stockHouse: StockHouse): Behavior[Message] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case FillOrder(products, replyTo) =>
          // check which products are available
          val availableProducts = products.filter { case (product, quantity) =>
            stockHouse.products.getOrElse(product, 0) >= quantity
          }

          // remove products from stock house
          val newStockHouse = availableProducts.foldLeft(stockHouse) {
            case (acc, (product, quantity)) =>
              acc.removeProduct(product, quantity)
          }

          // simulate random delay
          Thread.sleep(scala.util.Random.nextInt(4000))
          replyTo ! ProcessingService.InStock(availableProducts, context.self)

          apply(newStockHouse)
        case GetLocation(replyTo) =>
          replyTo ! stockHouse.address
          Behaviors.same
      }
    }
}

object Main {
  sealed trait Message
  case object Start extends Message

  def apply(): Behavior[Message] = Behaviors.setup { context =>
    // spawn stock house services with StockHouse instances
    val stockHouseServices = (1 to 10).map { i =>
      val address =
        Point(scala.util.Random.nextInt(100), scala.util.Random.nextInt(100))
      val products = (1 to 5).map { i =>
        Product(s"item-$i") -> scala.util.Random.nextInt(5)
      }.toMap

      context.log.info(s"stock house $i at $address with $products")

      context.spawn(
        StockHouseService(StockHouse(products, address)),
        s"stockHouseService-$i"
      )
    }.toList

    // spawn processing service with stock house services
    val processingService: ActorRef[ProcessingService.Message] =
      context.spawn(ProcessingService(stockHouseServices), "processingService")

    Behaviors.receiveMessage { case Start =>
      // spawn random number of clients with random addresses
      val clients = (1 to 5).map { i =>
        val address =
          Point(scala.util.Random.nextInt(100), scala.util.Random.nextInt(100))
        val client = Client(s"client-$i", address)

        context.spawn(
          ClientService(client)(using processingService),
          client.name
        )
      }

      Behaviors.same
    }
  }

  @main def run(): Unit = {
    val system: ActorSystem[Message] =
      ActorSystem(Main(), "shoppingStore")

    system ! Start

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
