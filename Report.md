# Report

## Used

For the assignment, I chose to use Scala 3, Akka Streams, and Alpakka CSV.

## Architecture

The Stream processing looks like this:

`csv[1] ~> parse[2] ~> map_to_class[3] ~> group[4] ~> throttle[5] ~> buffer[6] ~> count[7] ~> print[8]`

1. Load the CSV file into a Source of ByteStrings
2. Parse the CSV into a Map of Headers to Values
3. Create class instances for each record from the Map
4. Group the instances by library name
5. Throttle the stream to 10 records per second
6. Buffer the stream to 5 records with Backpressure
7. Count the dependencies for each library
8. Print the results

For the count[7] stage, There's a `Balance` with 2 ports to distribute the stream to 2 Flow stages. Both Flow stages are identical and count the dependencies for each library including the type of dependency (e.g., compile, runtime). The results are then merged back into a single stream with a `Merge` stage.
