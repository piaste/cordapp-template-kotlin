package com.template.webserver

import com.template.flows.ProductIssueInitiator
import com.template.flows.ProductWorkInitiator
import com.template.states.ProductOperation
import com.template.states.ProductState
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.*
import net.corda.core.node.services.vault.builder
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Define your API endpoints here.
 */
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

    @GetMapping(value = "/buildProduct/{productCode}", produces = arrayOf("application/json"))
    private fun buildProduct(@PathVariable("productCode") productCode: String): List<ProductState> {

        val response =
                proxy.startFlow(::ProductIssueInitiator,
                        productCode,
                        "1234",
                        100.toBigDecimal(),
                        proxy.partiesFromName("PartyA", false).single())
                        .returnValue
                        .get()

        // return response.toString()
        return proxy.vaultTrackByCriteria(criteria = VaultQueryCriteria(Vault.StateStatus.ALL), contractStateType = ProductState::class.java)
                .snapshot.states
                .map { it.state.data }
                //.map { it.linearId.id.toString()  }
                .toList()
    }

    @PostMapping(value = "/operazioni/{idOperazionePrecedente}/append", produces = arrayOf("application/json"))
    private fun buildProduct(@PathVariable("idOperazionePrecedente") idOperazionePrecedente: String,
                             @RequestBody operation: ProductOperation): List<ProductState> {

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
        return proxy.vaultTrackByCriteria(criteria = VaultQueryCriteria(Vault.StateStatus.ALL), contractStateType = ProductState::class.java)
                .snapshot.states
                .map { it.state.data }
                //.map { it.linearId.id.toString()  }
                .toList()

    }



    @GetMapping(value = "/blockchain", produces = arrayOf("application/json"))
    private fun GetBlockchain(@RequestParam("idArticolo") idArticolo: String?,
                              @RequestParam("idPuntoVendita") idPuntoVendita: String?,
                              @RequestParam("lotto") lotto: String?): List<Map<String, Any>> {

        val results  = builder {

            var criteria : QueryCriteria = VaultQueryCriteria(Vault.StateStatus.ALL)

            if( !lotto.isNullOrBlank()) {
//                criteria = criteria.and(VaultCustomQueryCriteria(ProductSchemaV1.PersistentProductState::batchCode.equal(lotto)))
            }

            if( !idArticolo.isNullOrBlank()) {
//                criteria = criteria.and(VaultCustomQueryCriteria(ProductSchemaV1.PersistentProductState::productCode.equal(idArticolo)))
            }

            if( !idPuntoVendita.isNullOrBlank()) {
//                criteria = criteria.and(VaultCustomQueryCriteria(ProductSchemaV1.PersistentProductState::owner.equal(idPuntoVendita)))
            }

            proxy.vaultTrackByCriteria(criteria = criteria, contractStateType = ProductState::class.java)
        }

        return results.snapshot.states
                .map { it.state.data }
                .filter { lotto.isNullOrBlank() || it.batchCode == lotto}
                .filter { idArticolo.isNullOrBlank() || it.productCode == idArticolo}
                .filter { idPuntoVendita.isNullOrBlank() || it.owner.name.organisation == idPuntoVendita}
                                       .map { mapOf(
                                               "idArticolo" to it.productCode,
                                               "idLotto" to it.batchCode,
                                               "quantità" to it.quantity.toPlainString(),
                                               "proprietario" to it.owner.name.organisation)
                                       }
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

}

