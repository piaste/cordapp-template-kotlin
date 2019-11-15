package com.template.states


import com.template.contracts.*
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.math.BigDecimal
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.engine.spi.CascadeStyles.PERSIST
import javax.persistence.*

@CordaSerializable
data class ProductOperation
  ( val description : String,
    val nature : String,
    val operator : String
  )
/* { fun toPersist(parent : ProductState) = ProductSchemaV1.PersistentOperationState(description, nature, operator, parent) }
*/



// *********
// * State *
// *********

@BelongsToContract(ProductContract::class)
data class ProductState
    ( val productCode: String
    , val batchCode: String
    , val quantity: BigDecimal
    , val owner: Party
    , val history : List<ProductOperation>
    , override val linearId : UniqueIdentifier
    , override val participants: List<AbstractParty> = listOf(owner)
    ) : LinearState
/*, QueryableState {
    *//**
     * Enumerate the schemas this state can export representations of itself as.
     *//*
    override fun supportedSchemas() = listOf(ProductSchemaV1)

    *//**
     * Export a representation for the given schema.
     *//*
    override fun generateMappedObject(schema: MappedSchema) = ProductSchemaV1.PersistentProductState(
        productCode, batchCode, quantity, owner.name.toString(), history.map {it.toPersist(this) }.toMutableList()
    )
}


/**
 * An object used to fully qualify the [CashSchema] family name (i.e. independent of version).
 */
object ProductSchema

/**
 * First version of a cash contract ORM schema that maps all fields of the [Cash] contract state as it stood
 * at the time of writing.
 */ // SQL column length
@CordaSerializable
object ProductSchemaV1 : MappedSchema(schemaFamily = ProductSchema.javaClass, version = 1, mappedTypes = listOf(PersistentProductState::class.java, PersistentOperationState::class.java)) {

    override val migrationResource = "cash.changelog-master"

    @Entity
    @Table(name = "products")
    class PersistentProductState(
            /** X500Name of owner party **/
            @Column(name = "productCode")
            var description: String,

            @Column(name = "batchCode")
            var batchCode: String,

            @Column(name = "quantity")
            var quantity: BigDecimal,

            @Column(name = "owner")
            var owner: String,

            /**
             * The @OneToMany annotation specifies a one-to-many relationship between this class and a collection included as a field.
             * The @JoinColumn and @JoinColumns annotations specify on which columns these tables will be joined on.
             */

            @JoinColumns(JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"),
                         JoinColumn(name = "output_index", referencedColumnName = "output_index"))
            @OneToMany(fetch = FetchType.EAGER)
            @Cascade(CascadeType.PERSIST)
            var history: MutableList<PersistentOperationState>

    ) : PersistentState()

    @Entity
    @Table(name = "operations")
    class PersistentOperationState(
            /** X500Name of owner party **/
            @Column(name = "description")
            var description : String,
            @Column(name = "nature")
            var nature : String,
            @Column(name = "operator")
            var operator : String,

            @ManyToOne(targetEntity = PersistentProductState::class)
            var persistentParentToken: ProductState

    ) : PersistentState()
}

        */