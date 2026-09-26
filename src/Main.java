import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Sistema de Gestión de Biblioteca - Módulo de Procesamiento Funcional
 * Desarrollado en Java Puro (Records, Streams, Lambdas e Inmutabilidad)
 */
public class Main {

    // -------------------------------------------------------------------------
    // 1. ESTRUCTURAS DE DATOS INMUTABLES (RECORDS Y ENUMS)
    // -------------------------------------------------------------------------

    public enum EstadoPrestamo { ACTIVO, DEVUELTO, EN_ATRASO }

    public record Autor(String id, String nombre, String nacionalidad) {}

    public record Libro(String isbn, String titulo, Autor autor, String categoria, int anioPublicacion) {}

    public record LibroDigital(Libro libro, String formato, double tamanoMB, String urlDescarga) {}

    public record Persona(String id, String nombre, String correo) {}

    public record Prestamo(
            String idPrestamo,
            Persona usuario,
            Libro libro,
            LocalDate fechaPrestamo,
            LocalDate fechaDevolucionEsperada,
            LocalDate fechaDevolucionReal,
            EstadoPrestamo estado
    ) {}

    public record Biblioteca(String nombre, List<Prestamo> prestamos, List<LibroDigital> catalogoDigital) {}

    // -------------------------------------------------------------------------
    // 2. MÉTODO PRINCIPAL
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        Biblioteca biblioteca = generarDatosBibliotecaSimulados();

        System.out.println("========================================================================");
        System.out.println("          SISTEMA DE BIBLIOTECA - PROCESAMIENTO FUNCIONAL              ");
        System.out.println("========================================================================\n");

        System.out.println("--- a) TOTAL DE PRÉSTAMOS POR CATEGORÍA ---");
        prestamosPorCategoria(biblioteca.prestamos())
                .forEach((cat, total) -> System.out.printf("Categoría %-15s: %d préstamos%n", cat, total));

        System.out.println("\n--- b) USUARIOS CON PRÉSTAMOS EN ATRASO ---");
        usuariosConAtrasos(biblioteca.prestamos())
                .forEach(usuario -> System.out.printf("Usuario Moroso: %-20s (Correo: %s)%n", usuario.nombre(), usuario.correo()));

        System.out.println("\n--- c) LIBROS MÁS SOLICITADOS (ORDEN DESCENDENTE) ---");
        librosMasSolicitados(biblioteca.prestamos())
                .forEach((titulo, total) -> System.out.printf("Libro %-30s: %d préstamos%n", titulo, total));

        System.out.println("\n--- d) HISTORIAL DE LIBROS LEÍDOS POR USUARIO ---");
        historialPorUsuario(biblioteca.prestamos())
                .forEach((usuario, libros) -> System.out.printf("Usuario %-15s: %s%n", usuario, String.join(", ", libros)));

        System.out.println("\n--- e) TIEMPO PROMEDIO DE PRÉSTAMO POR CATEGORÍA (DÍAS) ---");
        tiempoPromedioPrestamoPorCategoria(biblioteca.prestamos())
                .forEach((cat, dias) -> System.out.printf("Categoría %-15s: %.2f días promedio%n", cat, dias));

        long umbralDemanda = 2;
        System.out.println("\n--- f) CATEGORÍAS EN ALTA DEMANDA (UMBRAL > " + umbralDemanda + " PRÉSTAMOS) ---");
        detectarAltaDemandaCategorias(biblioteca.prestamos(), umbralDemanda)
                .forEach((cat, estado) -> System.out.printf("Categoría %-15s: ESTADO %s%n", cat, estado));
    }

    // -------------------------------------------------------------------------
    // 3. IMPLEMENTACIÓN DE REQUERIMIENTOS (FUNCIONES PURAS)
    // -------------------------------------------------------------------------

    public static Map<String, Long> prestamosPorCategoria(List<Prestamo> prestamos) {
        return prestamos.stream()
                .collect(Collectors.groupingBy(p -> p.libro().categoria(), Collectors.counting()));
    }

    public static List<Persona> usuariosConAtrasos(List<Prestamo> prestamos) {
        return prestamos.stream()
                .filter(p -> p.estado() == EstadoPrestamo.EN_ATRASO)
                .map(Prestamo::usuario)
                .distinct()
                .toList();
    }

    public static Map<String, Long> librosMasSolicitados(List<Prestamo> prestamos) {
        return prestamos.stream()
                .collect(Collectors.groupingBy(p -> p.libro().titulo(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static Map<String, List<String>> historialPorUsuario(List<Prestamo> prestamos) {
        return prestamos.stream()
                .sorted(Comparator.comparing(Prestamo::fechaPrestamo))
                .collect(Collectors.groupingBy(
                        p -> p.usuario().nombre(),
                        Collectors.mapping(p -> p.libro().titulo(), Collectors.toList())
                ));
    }

    public static Map<String, Double> tiempoPromedioPrestamoPorCategoria(List<Prestamo> prestamos) {
        return prestamos.stream()
                .filter(p -> p.fechaDevolucionReal() != null)
                .collect(Collectors.groupingBy(
                        p -> p.libro().categoria(),
                        Collectors.averagingDouble(p -> ChronoUnit.DAYS.between(p.fechaPrestamo(), p.fechaDevolucionReal()))
                ));
    }

    public static Map<String, String> detectarAltaDemandaCategorias(List<Prestamo> prestamos, long umbral) {
        Predicate<Long> esAltaDemanda = cantidad -> cantidad > umbral;

        return prestamos.stream()
                .collect(Collectors.groupingBy(p -> p.libro().categoria(), Collectors.counting()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> esAltaDemanda.test(entry.getValue()) ? "CRÍTICA (ALTA DEMANDA)" : "NORMAL"
                ));
    }

    // -------------------------------------------------------------------------
    // 4. GENERADOR DE DATOS SIMULADOS INMUTABLES
    // -------------------------------------------------------------------------

    private static Biblioteca generarDatosBibliotecaSimulados() {
        Autor autor1 = new Autor("A1", "Gabriel García Márquez", "Colombiana");
        Autor autor2 = new Autor("A2", "Robert C. Martin", "Estadounidense");
        Autor autor3 = new Autor("A3", "George Orwell", "Británica");

        Libro libro1 = new Libro("978-1", "Cien Años de Soledad", autor1, "Literatura", 1967);
        Libro libro2 = new Libro("978-2", "Clean Code", autor2, "Tecnología", 2008);
        Libro libro3 = new Libro("978-3", "1984", autor3, "Ciencia Ficción", 1949);
        Libro libro4 = new Libro("978-4", "El Amor en los Tiempos del Cólera", autor1, "Literatura", 1985);

        LibroDigital digital1 = new LibroDigital(libro2, "PDF", 15.5, "https://biblioteca.org/download/clean-code.pdf");

        Persona p1 = new Persona("U001", "Juan Pérez", "juan.perez@email.com");
        Persona p2 = new Persona("U002", "María Cortés", "maria.cortes@email.com");
        Persona p3 = new Persona("U003", "Carlos Gómez", "carlos.gomez@email.com");

        LocalDate hoy = LocalDate.now();

        List<Prestamo> prestamos = List.of(
                new Prestamo("PR01", p1, libro1, hoy.minusDays(20), hoy.minusDays(5), hoy.minusDays(4), EstadoPrestamo.DEVUELTO),
                new Prestamo("PR02", p1, libro2, hoy.minusDays(15), hoy.minusDays(1), null, EstadoPrestamo.EN_ATRASO),
                new Prestamo("PR03", p2, libro1, hoy.minusDays(10), hoy.plusDays(5), null, EstadoPrestamo.ACTIVO),
                new Prestamo("PR04", p2, libro4, hoy.minusDays(30), hoy.minusDays(15), hoy.minusDays(10), EstadoPrestamo.DEVUELTO),
                new Prestamo("PR05", p3, libro3, hoy.minusDays(25), hoy.minusDays(10), null, EstadoPrestamo.EN_ATRASO),
                new Prestamo("PR06", p3, libro2, hoy.minusDays(5), hoy.plusDays(10), null, EstadoPrestamo.ACTIVO)
        );

        return new Biblioteca("Biblioteca Central", prestamos, List.of(digital1));
    }
}