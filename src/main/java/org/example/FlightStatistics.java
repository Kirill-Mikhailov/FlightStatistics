package org.example;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class FlightStatistics {

    public static void main(String[] args) throws IOException {
        FlightStatistics flightStatistics = new FlightStatistics();


        Map<String, Duration> minDurationForCarriers = flightStatistics
                .findMinDurationForCarriers("VVO", "TLV", flightStatistics.readTickets().getTickets());

        double priceStatistics = flightStatistics.calculateStatistics("VVO", "TLV",
                flightStatistics.readTickets().getTickets());

        flightStatistics.writeAnswerInFile(minDurationForCarriers, priceStatistics);
        flightStatistics.printAnswer(minDurationForCarriers, priceStatistics);
    }

    public TicketsList readTickets() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("tickets.json");
        return objectMapper.readValue(file, TicketsList.class);
    }

    public Map<String, Duration> findMinDurationForCarriers(String origin, String destination, List<Ticket> tickets) {
         return tickets.stream()
                .filter(t -> t.getOrigin().equals(origin) && t.getDestination().equals(destination))
                .collect(Collectors.groupingBy(Ticket::getCarrier)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> findMinDuration(e.getValue()), (d1, d2) -> d1));
    }

    private Duration findMinDuration(List<Ticket> tickets) {
        return tickets.stream().map(t -> Duration.between(
                        LocalDateTime.of(t.getDepartureDate(), t.getDepartureTime()),
                        LocalDateTime.of(t.getArrivalDate(), t.getArrivalTime())))
                .min(Duration::compareTo).orElse(Duration.ZERO);
    }

    public double calculateStatistics(String origin, String destination, List<Ticket> tickets) {
        double[] list = tickets.stream().filter(t -> t.getOrigin().equals(origin) && t.getDestination().equals(destination)).map(Ticket::getPrice).mapToDouble(p -> p).toArray();
        Median median = new Median();
        return median.evaluate(list) - DoubleStream.of(list).average().orElse(0);
    }

    public void writeAnswerInFile(Map<String, Duration> minDurationForCarriers, double priceStatistics) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("answer.txt", StandardCharsets.UTF_8))) {
            bw.write("1) Минимальное время полета между городами Владивосток и Тель-Авив для каждого авиаперевозчика: "
                    + System.lineSeparator());
            for (Map.Entry<String, Duration> entry : minDurationForCarriers.entrySet()) {
                bw.write(entry.getKey() + " -> " + entry.getValue().toString().substring(2).toLowerCase());
                bw.newLine();
            }
            bw.write("2) Разницу между средней ценой и медианой для полета между городами " +
                    "Владивосток и Тель-Авив: " + priceStatistics);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printAnswer(Map<String, Duration> minDurationForCarriers, double priceStatistics) {
        System.out.println("1) Минимальное время полета между городами Владивосток и Тель-Авив для каждого авиаперевозчика: ");
        for (Map.Entry<String, Duration> entry : minDurationForCarriers.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue().toString().substring(2).toLowerCase());
        }
        System.out.println("2) Разницу между средней ценой и медианой для полета между городами " +
                "Владивосток и Тель-Авив: " + priceStatistics);
    }
}





















