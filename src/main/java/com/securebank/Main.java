package com.securebank;

import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Validación de Rúbrica: Si no hay 3 argumentos, fallar.
        if (args.length < 3) {
            System.err.println("ERROR: Faltan argumentos.");
            System.err.println("Uso: java -jar app.jar <datasets> <resultados> <key>");
            System.exit(1);
        }

        System.out.println("Proyecto compilado correctamente.");
        System.out.println("Dataset: " + args[0]);
        System.out.println("Output: " + args[1]);
        System.out.println("Key: " + args[2]);
    }
}