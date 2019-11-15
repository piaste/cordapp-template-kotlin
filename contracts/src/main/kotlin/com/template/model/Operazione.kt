package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable
import org.openapitools.model.Ingrediente
import org.openapitools.model.Soggetto

/**
 * 
 * @param dataOperazione Data in cui è stata effettuata l'operazione sul campo
 * @param natura Natura dell'operazione eseguita (eg. \"semina\")
 * @param descrizione Informazioni inserite dall'operatore riguardo l'operazione effettuata
 * @param appezzamentoOperazione Appezzamento sul quale è stata effettuata l'operazione
 * @param ingredienti Prodotti utilizzato nella operazione
 * @param soggetti 
 * @param dataRegistrazione Timestamp del momento in cui è stata effettuata l'operazione
 */
@CordaSerializable
data class Operazione (

        

        @JsonProperty("dataOperazione") val dataOperazione: java.time.LocalDate,

        @JsonProperty("natura") val natura: kotlin.String,

        @JsonProperty("descrizione") val descrizione: String,

        @JsonProperty("ingredienti") val ingredienti: kotlin.collections.List<Ingrediente>,

        @JsonProperty("soggetti") val soggetti: kotlin.collections.List<Soggetto>,

        @JsonProperty("dataRegistrazione") val dataRegistrazione: java.time.LocalDate,

        @JsonProperty("appezzamentoOperazione") val appezzamentoOperazione: kotlin.String? = null
)

enum class NaturaOperazione(val valore : String) {
        Trasformazione("Trasformazione"),
        TrasportoLotti("Trasporto lotti"),
        RicezioneLotti("Ricezione lotti"),
        Fertilizzazione("Fertilizzazione"),
        Aratura("Aratura"),
        Fresatura("Fresatura"),
        Semina("Semina"),
        FaseQuiescenza("Fase di quiescenza"),
        ConcimazioniFogliarie("Concimazioni fogliarie"),
        Raccolta("Raccolta"),
        Macinazione("Macinazione"),
        Produzione("Produzione"),
        TrasportoPuntoVendita("Trasporto al punto vendita")
}