package com.securebank;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    //Mapa en memoria
    private static final Map<Character, String> KEY_MAP = new HashMap<>();    

    public static void main(String[] args) {

        // Validación de Rúbrica: Si no hay 3 argumentos, fallar.
        if (args.length < 3) {
            System.err.println("ERROR: Faltan argumentos.");
            System.err.println("Uso: java -jar app.jar <datasets> <resultados> <key>");
            System.exit(1);
        }

        Path datasetPath = Paths.get(args[0]);
        Path resultadosPath = Paths.get(args[1]);
        Path keyPath = Paths.get(args[2]);

        try {
            //Asegurar la carpeta de resultados existe
            Files.createDirectories(resultadosPath);

            //Paso 1: Parseo y Normalizacion
            System.out.println("Procesanod archivos CSV...");
            List<String> filasUnificadas = procesarArchivos(datasetPath);

            //Guardar archivo intermedio (Requisito trazabilidad)
            Path pathUnificado = resultadosPath.resolve("operaciones_unificado.csv");
            guardarCSVUnificado(pathUnificado, filasUnificadas);
            System.out.println("Archivo unificado guardado en: " + pathUnificado);

            //TODO: Aqui irá la carga de llave encriptacion en el siguiente paso

        } catch (Exception e) {
            System.err.println("ERROR: Ocurrió un error durante la ejecución.");
            e.printStackTrace();
        }

    }

    // LÓGICA DE PARSEO Y NORMALIZACIÓN
    private static List<String> procesarArchivos(Path datasetsDir) throws IOException {
        List<String> listaUnificada = new ArrayList<>();

        //Archivo 1: operaciones_sucursales.csv (Ya tiene formate base)
        Path archivo1 = datasetsDir.resolve("operaciones_sucursales.csv");
        if (Files.exists(pathArchivo1)) {
            List<String> lineas1 = Files.readAllLines(pathArchivo1);
            boolean header1 = true;
            for (String lineas : lineas1) {
                if (linea.trim.isEmpty()) continue; // Saltar líneas vacías
                if (header1) { header1 = false; continue; } // Saltar header
                listaUnificada.add(linea.trim());
            }
        } else {
            System.err.println("Advertencia: No se encontró " + pathArchivo1);
        }

        //Archivo 2: transferencias_externas.csv (Requiere transformación)
        Path archivo2 = datasetsDir.resolve("transferencias_externas.csv");
        if (Files.exists(pathArchivo2)) {
            List<String> lineas2 = Files.readAllLines(pathArchivo2);
            boolean header2 = true;

            for (String linea : lineas2) {
                if (linea.trim.isEmpty()) continue; // Saltar líneas vacías
                if (header2) { header2 = false; continue; } // Saltar header

                //Truco SENIOR: Split que ignora ; dentro de comillas
                String[] tokens = splitCSV(linea);

                if (tokens.length < 5) continue;

                //1 Separar nombre completo (Quitando Comillas si las hay)
                String nombreComple = tokens[0].replaceAll("\"", "").trim();
                int primerEspacio = nombreComple.indexOf(" ");

                String clienteStr = (primerEspacio == -1) ? nombreComple : nombreComple.substring(0, primerEspacio);
                String apellidoStr = (primerEspacio == -1) ? "" : nombreComple.substring(primerEspacio + 1);

                //2 Mapeo directo columnas
                String monto = tokens[1];
                String moneda = tokens[2];

                //3 Convertir TIMESTAMP a ISO YYYY-MM-DD
                String fechaOriginal = tokens[3];
                String fechaNormalizada = fechaOriginal;
                try {
                    // Usamos java.time para parsear la ISO 8601
                    Instant instant = Instant.parse(fechaOriginal);
                    fechaNormalizada = LocalDate.ofInstant(instant, ZoneId.of("UTC")).toString();
                } catch (DateTimeParseException e) {
                    // Si falla, mantenemos el original para no perder datos
                }

                String sucursal = tokens[4];

                // Reconstruir en formato del archivo 1: cliente;apellido;monto;moneda;fecha;sucursal
                String nuevaLinea = String.join(";", clienteStr, apellidoStr, monto, moneda, fechaNormalizada, sucursal);
                listaUnificada.add(nuevaLinea);   
            }       
        } else {
            System.err.println("Advertencia: No se encontró " + pathArchivo2);
        }
        return listaUnificada;
    }
    // REGEX VITAL: Divide por ; solo si a su derecha hay un número par de comillas
    // Esto significa que el ; NO está "encerrado" entre comillas.
    private static String[] splitCSV(String linea) {
        return linea.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    private static void guardarCSVUnificado(Path path, List<String> filas) throws IOException {
        List<String> output = new ArrayList<>();
        output.add("cliente;apellido;monto;moneda;fecha_operacion;sucursal"); // Header estandarizado
        output.addAll(filas);
        Files.write(path, output);
    }

}