package biblioteca;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

// ==========================================
// 1. INTERFAZ BASE
// ==========================================
interface CatalogoItem {
    String getTitulo();
    boolean getDisponibilidad();
    String mostrarDetalle();
}

// ==========================================
// 2. ENUMERACIÓN
// ==========================================
enum EstadoPrestamo {
    ACTIVO,
    DEVUELTO,
    VENCIDO
}

// ==========================================
// 3. CLASE ABSTRACTA PERSONA (SRP)
// ==========================================
abstract class Persona {
    private String id;
    private String nombre;
    private String email;

    public Persona(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }

    public abstract String mostrarInfo();
}

// ==========================================
// 4. CLASE AUTOR (Herencia de Persona & Agregación con Libro)
// ==========================================
class Autor extends Persona {
    private String nacionalidad;
    private List<Libro> libros;

    public Autor(String id, String nombre, String email, String nacionalidad) {
        super(id, nombre, email);
        this.nacionalidad = nacionalidad;
        this.libros = new ArrayList<>();
    }

    public String getNacionalidad() { return nacionalidad; }
    public List<Libro> getLibros() { return libros; }

    public void agregarLibro(Libro libro) {
        if (!libros.contains(libro)) {
            libros.add(libro);
        }
    }

    @Override
    public String mostrarInfo() {
        return "Autor [ID: " + getId() + ", Nombre: " + getNombre() + ", Email: " + getEmail() +
                ", Nacionalidad: " + nacionalidad + ", Libros publicados: " + libros.size() + "]";
    }
}

// ==========================================
// 5. CLASE LIBRO (Superclase - Principio OCP)
// ==========================================
class Libro implements CatalogoItem {
    private String id;
    private String titulo;
    private String isbn;
    private int anioPublicacion;
    private boolean disponible;
    private Autor autor;

    public Libro(String id, String titulo, String isbn, int anioPublicacion, Autor autor) {
        this.id = id;
        this.titulo = titulo;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
        this.autor = autor;
        this.disponible = true;
        if (autor != null) {
            autor.agregarLibro(this);
        }
    }

    public String getId() { return id; }
    @Override public String getTitulo() { return titulo; }
    public String getIsbn() { return isbn; }
    public int getAnioPublicacion() { return anioPublicacion; }
    @Override public boolean getDisponibilidad() { return disponible; }
    public void setDisponible(boolean estado) { this.disponible = estado; }
    public Autor getAutor() { return autor; }

    // Sobrecarga de método (Overloading)
    public void setDisponible(boolean estado, String motivo) {
        this.disponible = estado;
        System.out.println("Disponibilidad del libro '" + titulo + "' cambiada a: " + estado + ". Motivo: " + motivo);
    }

    @Override
    public String mostrarDetalle() {
        String nombreAutor = (autor != null) ? autor.getNombre() : "Desconocido";
        return "Libro [ID: " + id + ", Título: " + titulo + ", ISBN: " + isbn +
                ", Año: " + anioPublicacion + ", Autor: " + nombreAutor +
                ", Disponible: " + disponible + "]";
    }
}

// ==========================================
// 6. CLASE LIBRO DIGITAL (Herencia de Libro - Principio LSP)
// ==========================================
class LibroDigital extends Libro {
    private String urlDescarga;
    private String formatoArchivo;
    private double tamanioMB;

    public LibroDigital(String id, String titulo, String isbn, int anioPublicacion, Autor autor,
                        String urlDescarga, String formatoArchivo, double tamanioMB) {
        super(id, titulo, isbn, anioPublicacion, autor);
        this.urlDescarga = urlDescarga;
        this.formatoArchivo = formatoArchivo;
        this.tamanioMB = tamanioMB;
    }

    public String getUrlDescarga() { return urlDescarga; }
    public String getFormatoArchivo() { return formatoArchivo; }
    public double getTamanioMB() { return tamanioMB; }

    public void descargar() {
        System.out.println("Descargando archivo digital '" + getTitulo() + "' desde " + urlDescarga + " (" + tamanioMB + " MB)...");
    }

    // Sobrescritura de método (@Override)
    @Override
    public String mostrarDetalle() {
        return super.mostrarDetalle() + " [Digital - Formato: " + formatoArchivo + ", Tamaño: " + tamanioMB + " MB]";
    }
}

// ==========================================
// 7. CLASE USUARIO (Herencia de Persona)
// ==========================================
class Usuario extends Persona {
    private String numeroCuenta;
    private List<Prestamo> prestamos;

    public Usuario(String id, String nombre, String email, String numeroCuenta) {
        super(id, nombre, email);
        this.numeroCuenta = numeroCuenta;
        this.prestamos = new ArrayList<>();
    }

    public String getNumeroCuenta() { return numeroCuenta; }
    public List<Prestamo> getPrestamos() { return prestamos; }

    public void agregarPrestamo(Prestamo prestamo) {
        this.prestamos.add(prestamo);
    }

    @Override
    public String mostrarInfo() {
        return "Usuario [ID: " + getId() + ", Nombre: " + getNombre() + ", N° Cuenta: " + numeroCuenta +
                ", Préstamos registrados: " + prestamos.size() + "]";
    }
}

// ==========================================
// 8. CLASE PRESTAMO (Composición con Libro)
// ==========================================
class Prestamo {
    private String id;
    private Date fechaPrestamo;
    private Date fechaDevolucionEsperada;
    private Date fechaDevolucionReal;
    private EstadoPrestamo estado;
    private Libro libro;
    private Usuario usuario;

    public Prestamo(String id, Libro libro, Usuario usuario, Date fechaDevolucionEsperada) {
        this.id = id;
        this.libro = libro;
        this.usuario = usuario;
        this.fechaPrestamo = new Date();
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
        this.estado = EstadoPrestamo.ACTIVO;

        // Composición / Gestión de disponibilidad
        this.libro.setDisponible(false);
        this.usuario.agregarPrestamo(this);
    }

    public String getId() { return id; }
    public Date getFechaPrestamo() { return fechaPrestamo; }
    public Date getFechaDevolucionEsperada() { return fechaDevolucionEsperada; }
    public Date getFechaDevolucionReal() { return fechaDevolucionReal; }
    public EstadoPrestamo getEstado() { return estado; }
    public Libro getLibro() { return libro; }
    public Usuario getUsuario() { return usuario; }

    public void registrarDevolucion() {
        this.fechaDevolucionReal = new Date();
        this.estado = EstadoPrestamo.DEVUELTO;
        this.libro.setDisponible(true);
    }

    public int calcularDiasRetraso() {
        Date fechaReferencia = (fechaDevolucionReal != null) ? fechaDevolucionReal : new Date();
        if (fechaReferencia.after(fechaDevolucionEsperada)) {
            long diff = fechaReferencia.getTime() - fechaDevolucionEsperada.getTime();
            return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }
        return 0;
    }

    public String mostrarDetalle() {
        return "Préstamo [ID: " + id + " | Libro: " + libro.getTitulo() +
                " | Usuario: " + usuario.getNombre() + " | Estado: " + estado + "]";
    }
}

// ==========================================
// 9. CLASE BIBLIOTECA (Gestión General)
// ==========================================
class Biblioteca {
    private String nombre;
    private List<Libro> libros;
    private List<Usuario> usuarios;
    private List<Prestamo> prestamos;

    public Biblioteca(String nombre) {
        this.nombre = nombre;
        this.libros = new ArrayList<>();
        this.usuarios = new ArrayList<>();
        this.prestamos = new ArrayList<>();
    }

    public void registrarLibro(Libro libro) {
        libros.add(libro);
    }

    public void registrarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public Prestamo realizarPrestamo(Libro libro, Usuario usuario, Date fechaDevolucionEsperada) {
        if (!libro.getDisponibilidad()) {
            System.out.println("El libro '" + libro.getTitulo() + "' no está disponible para préstamo.");
            return null;
        }
        String idPrestamo = "PRES-" + (prestamos.size() + 1);
        Prestamo nuevoPrestamo = new Prestamo(idPrestamo, libro, usuario, fechaDevolucionEsperada);
        prestamos.add(nuevoPrestamo);
        System.out.println("Préstamo realizado con éxito. ID: " + idPrestamo);
        return nuevoPrestamo;
    }

    public void registrarDevolucion(Prestamo prestamo) {
        if (prestamo != null) {
            prestamo.registrarDevolucion();
            System.out.println("Devolución registrada correctamente para el préstamo: " + prestamo.getId());
        }
    }

    public List<Libro> buscarLibroPorTitulo(String titulo) {
        List<Libro> resultado = new ArrayList<>();
        for (Libro l : libros) {
            if (l.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                resultado.add(l);
            }
        }
        return resultado;
    }

    public List<Libro> buscarLibroPorAutor(String nombreAutor) {
        List<Libro> resultado = new ArrayList<>();
        for (Libro l : libros) {
            if (l.getAutor() != null && l.getAutor().getNombre().toLowerCase().contains(nombreAutor.toLowerCase())) {
                resultado.add(l);
            }
        }
        return resultado;
    }

    public List<Libro> listarLibrosDisponibles() {
        List<Libro> disponibles = new ArrayList<>();
        for (Libro l : libros) {
            if (l.getDisponibilidad()) {
                disponibles.add(l);
            }
        }
        return disponibles;
    }
}

// ==========================================
// 10. CLASE MAIN (Entrada Principal de Pruebas)
// ==========================================
public class Main {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE GESTIÓN DE BIBLIOTECA ===");

        // 1. Instanciación de la Biblioteca
        Biblioteca biblioteca = new Biblioteca("Biblioteca Central");

        // 2. Creación de Autores
        Autor autor1 = new Autor("A-01", "Gabriel García Márquez", "gabo@mail.com", "Colombiana");
        Autor autor2 = new Autor("A-02", "J.K. Rowling", "jk@mail.com", "Británica");

        // 3. Creación de Libros (Físicos y Digitales)
        Libro libro1 = new Libro("L-01", "Cien años de soledad", "978-0307474728", 1967, autor1);
        LibroDigital libro2 = new LibroDigital("L-02", "El coronel no tiene quien le escriba", "978-8420471839", 1961, autor1, "http://biblioteca.com/coronel.pdf", "PDF", 5.2);
        LibroDigital libro3 = new LibroDigital("L-03", "Harry Potter y la piedra filosofal", "978-8478886548", 1997, autor2, "http://biblioteca.com/hp1.epub", "EPUB", 12.8);

        // Registro de libros
        biblioteca.registrarLibro(libro1);
        biblioteca.registrarLibro(libro2);
        biblioteca.registrarLibro(libro3);

        // 4. Creación y Registro de Usuarios
        Usuario usuario1 = new Usuario("U-01", "Carlos Gómez", "carlos@mail.com", "CTA-1001");
        biblioteca.registrarUsuario(usuario1);

        // 5. Demostración de Polimorfismo
        System.out.println("\n--- CATÁLOGO DE LIBROS (POLIMORFISMO EN ACCIÓN) ---");
        List<Libro> catalogo = biblioteca.listarLibrosDisponibles();
        for (Libro item : catalogo) {
            System.out.println(item.mostrarDetalle()); // Llama al método sobrescrito dinámicamente
        }

        // 6. Demostración de Sobrecarga de Método
        System.out.println("\n--- DEMOSTRACIÓN DE SOBRECARGA ---");
        libro1.setDisponible(false, "Mantenimiento y reempastado");
        libro1.setDisponible(true, "Mantenimiento finalizado");

        // 7. Proceso de Préstamo
        System.out.println("\n--- REALIZAR PRÉSTAMO ---");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date fechaDevolucionEsperada = cal.getTime();

        Prestamo prestamo1 = biblioteca.realizarPrestamo(libro1, usuario1, fechaDevolucionEsperada);
        if (prestamo1 != null) {
            System.out.println(prestamo1.mostrarDetalle());
        }

        System.out.println("¿'Cien años de soledad' sigue disponible?: " + libro1.getDisponibilidad());

        // 8. Proceso de Devolución
        System.out.println("\n--- REGISTRO DE DEVOLUCIÓN ---");
        biblioteca.registrarDevolucion(prestamo1);
        System.out.println("¿'Cien años de soledad' está disponible nuevamente?: " + libro1.getDisponibilidad());

        // 9. Funcionalidad propia de LibroDigital
        System.out.println("\n--- DESCARGA DE LIBRO DIGITAL ---");
        libro2.descargar();
    }
}