package org.openapitools.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
/**
 * 
 * @param operatore Nome dell'operatore che ha effettuato l'operazione
 * @param azienda Azienda alla quale appartiene l'operatore
 * @param localit Luogo dove o verso il quale è stata effettuata l'operazione
 */
data class Soggetto (

        @JsonProperty("operatore") val operatore: kotlin.String,

        @JsonProperty("azienda") val azienda: kotlin.String,

        @JsonProperty("località") val localit: kotlin.String
) {

}

