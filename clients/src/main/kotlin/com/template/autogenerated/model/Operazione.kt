package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import org.openapitools.model.Ingrediente
import org.openapitools.model.Soggetto
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

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
data class Operazione (

        @get:NotNull 
        @JsonProperty("dataOperazione") val dataOperazione: java.time.LocalDate,

        @get:NotNull 
        @JsonProperty("natura") val natura: kotlin.String,

        @get:NotNull 
        @JsonProperty("descrizione") val descrizione: kotlin.String,

        @get:NotNull 
        @JsonProperty("ingredienti") val ingredienti: kotlin.collections.List<Ingrediente>,

        @get:NotNull 
        @JsonProperty("soggetti") val soggetti: kotlin.collections.List<Soggetto>,

        @get:NotNull 
        @JsonProperty("dataRegistrazione") val dataRegistrazione: java.time.LocalDate,

        @JsonProperty("appezzamentoOperazione") val appezzamentoOperazione: kotlin.String? = null
) {

}

