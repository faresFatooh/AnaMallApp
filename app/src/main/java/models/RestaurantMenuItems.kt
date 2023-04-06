package models

public class RestaurantMenuItems {
    private var name: String = ""
    private var price: String = ""
    private var specification: String = ""
    private var category: String = ""
    private var is_active: String = ""
    private var is_added: String = ""
    private var description: String = ""
    private var call: String = ""

    constructor(
        name: String,
        price: String,
        specification: String,
        category: String,
        is_active: String,
        is_added: String,
        description: String,
        call: String
    ) {
        this.name = name
        this.price = price
        this.specification = specification
        this.category = category
        this.is_active = is_active
        this.is_added = is_added
        this.description = description
        this.call = call
    }

    constructor() {}


    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }
    fun getCall(): String {
        return call
    }

    fun setColl(call: String) {
        this.call = call
    }

    fun getPrice(): String {
        return price
    }

    fun setPrice(price: String) {
        this.price = price
    }

    fun getSpecification(): String {
        return specification
    }

    fun setSpecification(specification: String) {
        this.specification = specification
    }

    fun getCategory(): String {
        return category
    }

    fun setCategory(category: String) {
        this.category = category
    }

    fun getIs_active(): String {
        return is_active
    }

    fun setIs_active(is_active: String) {
        this.is_active = is_active
    }

    fun getIs_added(): String {
        return is_added
    }

    fun setIs_added(is_added: String) {
        this.is_added = is_added
    }
    fun getDescription(): String {
        return description
    }

    fun setDescription(description: String) {
        this.description = description
    }


}

