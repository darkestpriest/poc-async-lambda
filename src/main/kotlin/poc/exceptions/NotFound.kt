package poc.exceptions

class NotFound(itemsBody: String): RuntimeException("cannot find entity by $itemsBody")