package com.template.webserver

import com.template.flows.ProductIssueInitiator
import com.template.flows.ProductWorkInitiator
import com.template.states.ProductOperation
import com.template.states.ProductState
import org.openapitools.model.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.internal.toMultiMap
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.*
import net.corda.core.node.services.vault.builder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull


/*data class ProductStateDTO
            ( val productCode: String
            , val batchCode: String
            , val quantity: BigDecimal
            , val owner: String
            , val history : List<ProductOperation>
            , val id : UUID
            , val participants: List<String>
    ) {
    constructor(ps : ProductState) : this(ps.productCode, ps.batchCode, ps.quantity, ps.owner.name.organisation, ps.history, ps.linearId.id,
            ps.participants.map { it.nameOrNull()?.toString() ?: "" })
}*/

data class ArticoloDTO
(val idArticolo : String,
 val descrizione : String,
 val lottiEsistenti : List<String>)

data class ProductOperationDTO
    ( val descrizione : String
    , val natura : String
    , val operatore : String
    , val id : UUID
    )


data class ProductStateDTO
( val idArticolo: String
  , val lotto: String
  , val operazioni : List<ProductOperationDTO>
) {
    constructor(ps : ProductState) : this(
            idArticolo =ps.productCode,
            lotto = ps.batchCode,
            operazioni =
                ps.history.map {
                    ProductOperationDTO(
                        descrizione = it.description,
                        natura = it.nature,
                        operatore = it.operator,
                        id = ps.linearId.id
                )
            }
    )

    constructor(sar : StateAndRef<ProductState>) : this(sar.state.data) {
        var ps = sar.state.data
    }
}

/**
 * Define your API endpoints here.
 */
@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy


    @GetMapping(value = "/templateendpoint", produces = arrayOf("text/plain"))
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @PostMapping(value = "/blockchain", produces = arrayOf("application/json"))
    private fun PostBlockchain(@RequestParam("idArticolo") idArticolo: String,
                              @RequestParam("lotto") lotto: String): List<ProductStateDTO>
    {

        val response =
                proxy.startFlow(::ProductIssueInitiator,
                        idArticolo,
                        lotto,
                        100.toBigDecimal(),
                        proxy.partiesFromName("PartyA", false).single())
                        .returnValue
                        .get()

        // return response.toString()
        return proxy.vaultTrackByCriteria(criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED), contractStateType = ProductState::class.java)
                .snapshot.states
                .map { ProductStateDTO(it.state.data    ) }
                //.map { it.linearId.id.toString()  }
                .toList()
    }

    @PostMapping(value = "/operazioni/{idOperazionePrecedente}/append", produces = arrayOf("application/json"))
    private fun buildProduct(@PathVariable("idOperazionePrecedente") idOperazionePrecedente: String,
                             @RequestBody operation: ProductOperation): List<ProductStateDTO> {

        val response =
                proxy.startFlow(::ProductWorkInitiator,
                        UUID.fromString(idOperazionePrecedente),
                        operation.description,
                        operation.nature,
                        operation.operator,
                        proxy.partiesFromName("PartyA", false).single())
                        .returnValue
                        .get()

        // return response.toString()
        return proxy.vaultTrackByCriteria(criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED), contractStateType = ProductState::class.java)
                .snapshot.states
                .map { ProductStateDTO(it.state.data) }
                //.map { it.linearId.id.toString()  }
                .toList()

    }

    @GetMapping(value = "/articoli", produces = arrayOf("application/json"))
    private fun GetArticoli() : List<ArticoloDTO> {
        return proxy.vaultTrackByCriteria(criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED), contractStateType = ProductState::class.java)
                .snapshot.states
                .map { Pair(it.state.data.productCode, it.state.data.batchCode) }
                .toMultiMap()
                .map { ArticoloDTO( idArticolo = it.key , descrizione = "TODO", lottiEsistenti = it.value ) }
    }


    @GetMapping(value = "/blockchain", produces = arrayOf("application/json"))
    private fun GetBlockchain(@RequestParam("idArticolo") idArticolo: String?,
                              @RequestParam("idPuntoVendita") idPuntoVendita: String?,
                              @RequestParam("lotto") lotto: String?): List<ProductStateDTO> {

        val results = builder {

            var criteria: QueryCriteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

            if (!lotto.isNullOrBlank()) {
//                criteria = criteria.and(VaultCustomQueryCriteria(ProductSchemaV1.PersistentProductState::batchCode.equal(lotto)))
            }

            if (!idArticolo.isNullOrBlank()) {
//                criteria = criteria.and(VaultCustomQueryCriteria(ProductSchemaV1.PersistentProductState::productCode.equal(idArticolo)))
            }

            if (!idPuntoVendita.isNullOrBlank()) {
//                criteria = criteria.and(VaultCustomQueryCriteria(ProductSchemaV1.PersistentProductState::owner.equal(idPuntoVendita)))
            }

            proxy.vaultTrackByCriteria(criteria = criteria, contractStateType = ProductState::class.java)
        }

        return results.snapshot.states
                .map { it.state.data }
                .filter { lotto.isNullOrBlank() || it.batchCode == lotto }
                .filter { idArticolo.isNullOrBlank() || it.productCode == idArticolo }
                .filter { idPuntoVendita.isNullOrBlank() || it.owner.name.organisation == idPuntoVendita }
                .map { ProductStateDTO(it) }
        /*mapOf(
                                               "idArticolo" to it.productCode,
                                               "idLotto" to it.batchCode,
                                               "quantità" to it.quantity.toPlainString(),
                                               "proprietario" to it.owner.name.organisation)
                                       }

            */
    }
//    /**
//     * Ritorna la lista degli idArticolo e descrizioni gestiti dalla blockchain
//     *
//     */
//    @Location("/articoli") class articoliGet()
//
//    /**
//     * Ritorna tutte le blockchain che interessano la merce identificata dai parametri di query
//     *
//     * @param idArticolo
//     * @param idPuntoVendita  (optional)
//     * @param lotto  (optional)
//     */
//    @Location("/blockchain") class blockchainGet(val idArticolo: kotlin.String, val idPuntoVendita: kotlin.String, val lotto: kotlin.String)
//
//    /**
//     * Inizia una nuova blockchain associata ad un articolo e lotto
//     *
//     * @param idArticolo
//     * @param lotto
//     * @param body  (optional)
//     */
//    @Location("/blockchain") class blockchainPost(val idArticolo: kotlin.String, val lotto: kotlin.String, val body: Operazione)
//
//    /**
//     * Aggiunge una nuova operazione subito dopo l&#x27;operazione specificata
//     *
//     * @param idOperazionePrecedente Id numerico dell&#x27;operazione a cui agganciare l&#x27;operazione fornita
//     * @param body  (optional)
//     */
//    @Location("/operazioni/{idOperazionePrecedente}/append") class operazioniIdOperazionePrecedenteAppendPost(val idOperazionePrecedente: kotlin.String, val body: NuovaOperazione)

    @RestController
    @Validated
    @RequestMapping("\${api.base-path:/v1}")
    class DefaultApiController() {


        @RequestMapping(
                value = ["/natureoperazioni"],
                produces = ["application/json"],
                method = [RequestMethod.GET])
        fun natureoperazioniGet(): ResponseEntity<List<String>> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }


        @RequestMapping(
                value = ["/storytelling/{idArticolo}/{natura}/images"],
                produces = ["application/json"],
                method = [RequestMethod.GET])
        fun storytellingIdArticoloNaturaImagesGet( @PathVariable("idArticolo") idArticolo: String
                                                   , @PathVariable("natura") natura: String
        ): ResponseEntity<List<String>> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }


        @RequestMapping(
                value = ["/storytelling/{idArticolo}/{natura}/overrideText"],
                produces = ["text/plain"],
                method = [RequestMethod.GET])
        fun storytellingIdArticoloNaturaOverrideTextGet( @PathVariable("idArticolo") idArticolo: String
                                                         , @PathVariable("natura") natura: String
        ): ResponseEntity<String> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }


        @RequestMapping(
                value = ["/storytelling/{idArticolo}/{natura}/text"],
                produces = ["text/plain"],
                method = [RequestMethod.GET])
        fun storytellingIdArticoloNaturaTextGet( @PathVariable("idArticolo") idArticolo: String
                                                 , @PathVariable("natura") natura: String
        ): ResponseEntity<String> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }
    }

    @RestController
    @Validated
    @RequestMapping("\${api.base-path:/v1}")
    class ProduttoreApiController() {


        @RequestMapping(
                value = ["/articoli"],
                produces = ["application/json"],
                method = [RequestMethod.GET])
        fun articoliGet(): ResponseEntity<List<Articolo>> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }


        @RequestMapping(
                value = ["/blockchain"],
                produces = ["application/json"],
                method = [RequestMethod.GET])
        fun blockchainGet(@NotNull  @RequestParam(value = "idArticolo", required = true) idArticolo: String
                          , @RequestParam(value = "idPuntoVendita", required = false) idPuntoVendita: String?
                          , @RequestParam(value = "lotto", required = false) lotto: String?
        ): ResponseEntity<List<Blockchain>> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }


        @RequestMapping(
                value = ["/blockchain/{idOperazionePrecedente}"],
                produces = ["application/json"],
                consumes = ["application/json"],
                method = [RequestMethod.POST])
        fun blockchainIdOperazionePrecedentePost( @PathVariable("idOperazionePrecedente") idOperazionePrecedente: String
                                                  , @Valid @RequestBody operazione: Operazione?
        ): ResponseEntity<String> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }


        @RequestMapping(
                value = ["/blockchain"],
                produces = ["application/json"],
                consumes = ["application/json"],
                method = [RequestMethod.POST])
        fun blockchainPost(@NotNull  @RequestParam(value = "idArticolo", required = true) idArticolo: String
                           ,@NotNull @RequestParam(value = "lotto", required = true) lotto: String
                           , @Valid @RequestBody operazione: Operazione?
        ): ResponseEntity<String> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }
    }


    @RestController
    @Validated
    @RequestMapping("\${api.base-path:/v1}")
    class ConsumerApiController() {


        @RequestMapping(
                value = ["/blockchain"],
                produces = ["application/json"],
                method = [RequestMethod.GET])
        fun blockchainGet(@NotNull @RequestParam(value = "idArticolo", required = true) idArticolo: String
                          , @RequestParam(value = "idPuntoVendita", required = false) idPuntoVendita: String?
                          , @RequestParam(value = "lotto", required = false) lotto: String?
        ): ResponseEntity<List<Blockchain>> {
            return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
        }
    }



}

