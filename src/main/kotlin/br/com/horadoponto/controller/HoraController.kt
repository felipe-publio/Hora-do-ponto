package br.com.horadoponto.controller


import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom
import com.google.gson.GsonBuilder
import org.springframework.web.bind.annotation.*


@RequestMapping("/api/horadoponto")
@RestController
class HoraController {

    @RequestMapping(value = "/json", method = [RequestMethod.GET], produces = ["application/json"])
    fun resthora(@RequestParam(value = "entrada", defaultValue = "10") entrada: Int, @RequestParam(value = "saida", defaultValue = "17") saida: Int,
                 @RequestParam(value = "mes", defaultValue = "01") mes: Int, @RequestParam(value = "ano", defaultValue = "10") ano: Int): String {
        return getHoras(entrada, saida, mes, ano)
    }

    fun getHoras(entrada: Int, saida: Int, mes: Int, ano: Int): String {

        var data = ""

        if (mes < 10) {
            data = "$ano-0$mes-01"
        } else {
            data = "$ano-$mes-01"
        }

        val dataReferencia = LocalDate.parse(data, DateTimeFormatter.ISO_DATE)
        val saidaAlmoco = LocalTime.of(12, 0, 0) //Hora de almoço fixa
        val voltaAlmoco = LocalTime.of(13, 0, 0)
        val horaEntrada: LocalTime = LocalTime.of(entrada, 0, 0)
        val horaSaida: LocalTime = LocalTime.of(saida, 0, 0)

        var folhaDePonto: MutableList<FolhaDePonto?> = mutableListOf()

        for (day in 0 until dataReferencia.lengthOfMonth()) {

            if (dataReferencia.plusDays((day).toLong()).dayOfWeek.toString() == "SATURDAY" || dataReferencia.plusDays((day).toLong()).dayOfWeek.toString() == "SUNDAY") {
                folhaDePonto.add(day, FolhaDePonto(
                        dia = dataReferencia.plusDays((day).toLong()).toString(), hora_entrada = "",
                        hora_saida = "", almoco_saida = "",
                        almoco_volta = "", dia_semana = dataReferencia.plusDays((day).toLong()).dayOfWeek.toString()))
            } else {
                var horaEntradaPonto = randomazi(horaEntrada)
                val horaSaidaPonto = randomazi(horaSaida)
                if (horaEntradaPonto == horaSaidaPonto) horaEntradaPonto = randomazi(horaEntrada)

                var almocoSaidaPonto = randomazi(saidaAlmoco)
                val almocoVoltaPonto = randomazi(voltaAlmoco)
                if (almocoSaidaPonto == almocoVoltaPonto) almocoSaidaPonto = randomazi(saidaAlmoco)

                folhaDePonto.add(day, FolhaDePonto(
                        dia = dataReferencia.plusDays((day).toLong()).toString(), hora_entrada = horaEntradaPonto.toString(),
                        hora_saida = horaSaidaPonto.toString(), almoco_saida = almocoSaidaPonto.toString(),
                        almoco_volta = almocoVoltaPonto.toString(), dia_semana = dataReferencia.plusDays((day).toLong()).dayOfWeek.toString()))
            }
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonInString = gson.toJson(folhaDePonto)

        return jsonInString.toString()
    }

    fun randomazi(hora: LocalTime): LocalTime {

        val random = ThreadLocalRandom.current().nextLong(1, 14)
        val result : LocalTime
        result = if (random.toInt() % 2 == 0) {
            hora.plusMinutes(random)
        } else {
            hora.minusMinutes(random)
        }

        return if (result == hora){
             randomazi(hora)
        }else result
    }

    data class FolhaDePonto(
            var dia: String,
            var dia_semana: String,
            var hora_entrada: String,
            var hora_saida: String,
            var almoco_saida: String,
            var almoco_volta: String
    )
}