package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * 
 * @param operatore Nome dell'operatore che ha effettuato l'operazione
 * @param azienda Azienda alla quale appartiene l'operatore
 * @param localit Luogo dove o verso il quale è stata effettuata l'operazione
 */
data class Soggetto (

        @get:NotNull 
        @JsonProperty("operatore") val operatore: kotlin.String,

        @get:NotNull 
        @JsonProperty("azienda") val azienda: kotlin.String,

        @get:NotNull 
        @JsonProperty("località") val localit: kotlin.String
) {

}

