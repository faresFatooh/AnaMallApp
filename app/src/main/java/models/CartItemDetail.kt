package models

class CartItemDetail {
    private var select_name: String = ""
    private var select_price: String = ""
    private var select_specification: String = ""
    private var item_count: String = ""

    constructor(
        select_name: String,
        select_price: String,
        select_specification: String,
        item_count: String
    ) {
        this.select_name = select_name
        this.select_price = select_price
        this.select_specification = select_specification
        this.item_count = item_count
    }

    constructor()

    fun getSelect_name(): String {
        return select_name
    }

    fun setSelect_name(select_name: String) {
        this.select_name = select_name
    }

    fun getSelect_price(): String {
        return select_price
    }

    fun setSelect_price(select_price: String) {
        this.select_price = select_price
    }

    fun getSelect_specification(): String {
        return select_specification
    }

    fun setSelect_specification(select_specification: String) {
        this.select_specification = select_specification
    }

    fun getItem_count(): String {
        return item_count
    }

    fun setItem_count(item_count: String) {
        this.item_count = item_count
    }

}
