package com.template.webserver

import com.template.flows.ProductIssueInitiator
import com.template.flows.ProductWorkInitiator
import org.openapitools.model.*
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull


/**
 * Define your API endpoints here.
 */
@CrossOrigin(origins = ["*"])
@RestController
@Validated
// @RequestMapping("\${api.base-path:/v1}")
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
        const val OWNER = "PartyA"

        private val AnagraficaArticolo = mapOf(
                "PNINTSES" to "pane integrale con semi di sesamo, iposodico, normale"
                , "PNINTGIR" to "pane integrale con semi di lino e girasole"
                , "TRTMIGN" to "tortini mignon"
                , "PLUMCK" to "plum-cake"
                , "TRTDLC" to "torte dolci"
                , "STRDL" to "strudel"
                , "CRST" to "crostate e crostatine"
                , "BSCT" to "biscotti"
                , "TRTSLT" to "torte salate"
        )
    }

    private val proxy = rpc.proxy

    private fun trackStates() =
            proxy.vaultTrackByCriteria(
                    criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED),
                    contractStateType = Blockchain::class.java)



    private fun queryStates() =
            proxy.vaultQueryByCriteria(
                    criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED),
                    contractStateType = Blockchain::class.java)
                    .states



    @GetMapping(value = ["/templateendpoint"], produces = ["text/plain"])
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @RequestMapping(
            value = ["/natureoperazioni"],
            produces = ["application/json"],
            method = [RequestMethod.GET])
    fun natureoperazioniGet(): ResponseEntity<List<String>> {
        return ResponseEntity.ok (NaturaOperazione.values().map { it.valore })
    }


    @RequestMapping(
            value = ["/articoli"],
            produces = ["application/json"],
            method = [RequestMethod.GET])
    fun articoliGet(): ResponseEntity<List<Articolo>> {

        val defaults =
                AnagraficaArticolo.map { Pair(it.key, listOf<String>()) }.toMap()

        val stored =
                queryStates()
                        .map { Pair(it.state.data.idArticolo, it.state.data.idLotto) }
                        .toMultiMap()

        val fullList = defaults + stored

        return fullList
                .map {
                    Articolo(
                            idArticolo = it.key
                            , descrizione = AnagraficaArticolo.getOrDefault(it.key, "<???>")
                            , lottiEsistenti = it.value)
                }
                .let { ResponseEntity.ok(it) }

    }


    @RequestMapping(
            value = ["/blockchain"],
            produces = ["application/json"],
            method = [RequestMethod.GET])
    fun blockchainGet(@NotNull @RequestParam(value = "idArticolo", required = true) idArticolo: String
                      , @RequestParam(value = "idPuntoVendita", required = false) idPuntoVendita: String?
                      , @RequestParam(value = "lotto", required = false) lotto: String?
    ): ResponseEntity<List<Blockchain>> {

        val states =  queryStates()

        val lottoRicerca =
            if(lotto.isNullOrBlank() && idPuntoVendita?.isNotBlank() == true && idArticolo.isNotBlank()) {
                val x =
                        trackStates().
                            updates
                             .filter { it.produced.first().state.data.idArticolo == idArticolo }


        }


        return queryStates()
                .map { it.state.data }
                .filter { lotto.isNullOrBlank() || it.idLotto == lotto }
                .filter { idArticolo.isNullOrBlank() || it.idArticolo == idArticolo }
                .filter { idPuntoVendita.isNullOrBlank() || TODO == idPuntoVendita }
                .let { ResponseEntity.ok(it) }
    }


    @RequestMapping(
            value = ["/blockchain/{idOperazionePrecedente}"],
            produces = ["application/json"],
            consumes = ["application/json"],
            method = [RequestMethod.POST])
    fun blockchainIdOperazionePrecedentePost(@PathVariable("idOperazionePrecedente") idOperazionePrecedente: String
                                             , @Valid @RequestBody operazione: Operazione
    ): ResponseEntity<String> {


        val response =
                proxy.startFlow(::ProductWorkInitiator,
                        UUID.fromString(idOperazionePrecedente),
                        operazione,
                        proxy.partiesFromName(OWNER, false).single())
                        .returnValue
                        .get()

        // return response.toString()
        return proxy.vaultTrackByCriteria(criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED), contractStateType = Blockchain::class.java)
                .snapshot.states
                .map { it.state.data }
                //.map { it.linearId.id.toString()  }
                .toList()


    }

    @RequestMapping(
            value = ["/blockchain"],
            produces = ["application/json"],
            consumes = ["application/json"],
            method = [RequestMethod.POST])
    fun blockchainPost(@NotNull @RequestParam(value = "idArticolo", required = true) idArticolo: String
                       , @NotNull @RequestParam(value = "lotto", required = true) lotto: String
                       , @Valid @RequestBody operazione: Operazione?
    ): ResponseEntity<String> {

        val response =
                proxy.startFlow(::ProductIssueInitiator,
                        idArticolo,
                        lotto,
                        100.toBigDecimal(),
                        proxy.partiesFromName(OWNER, false).single())
                        .returnValue
                        .get()

        // return response.toString()
        return proxy.vaultTrackByCriteria(criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED), contractStateType = Blockchain::class.java)
                .snapshot.states
                .map { ProductStateDTO(it.state.data) }
                //.map { it.linearId.id.toString()  }
                .toList()

    }


    //// STORYTELLING




    @RequestMapping(
            value = ["/storytelling/{idArticolo}/{natura}/images"],
            produces = ["application/json"],
            method = [RequestMethod.GET])
    fun storytellingIdArticoloNaturaImagesGet(@PathVariable("idArticolo") idArticolo: String
                                              , @PathVariable("natura") natura: String
    ): ResponseEntity<List<String>> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }


    @RequestMapping(
            value = ["/storytelling/{idArticolo}/{natura}/overrideText"],
            produces = ["text/plain"],
            method = [RequestMethod.GET])
    fun storytellingIdArticoloNaturaOverrideTextGet(@PathVariable("idArticolo") idArticolo: String
                                                    , @PathVariable("natura") natura: String
    ): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }


    @RequestMapping(
            value = ["/storytelling/{idArticolo}/{natura}/text"],
            produces = ["text/plain"],
            method = [RequestMethod.GET])
    fun storytellingIdArticoloNaturaTextGet(@PathVariable("idArticolo") idArticolo: String
                                            , @PathVariable("natura") natura: String
    ): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }



}

