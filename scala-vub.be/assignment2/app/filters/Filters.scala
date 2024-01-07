package filters

import play.api.http.{DefaultHttpFilters, EnabledFilters}

import javax.inject.Inject

class Filters @Inject() (
    defaultFilters: EnabledFilters,
    authFilter: AuthFilter
) extends DefaultHttpFilters(defaultFilters.filters :+ authFilter: _*)
