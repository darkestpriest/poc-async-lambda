package poc.repository

import poc.model.domain.Item

data class SaveResponse(
        val item: Item,
        val dbResponse: String
)