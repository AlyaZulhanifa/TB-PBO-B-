import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.time.*;
import java.time.temporal.ChronoUnit;

// Antarmuka untuk menampilkan detail event
interface DetailEvent {
    void tampilkanDetail();
}

// Kelas induk Event
class Event {
    protected int id;
    protected String namaEvent;
    protected java.sql.Date tanggalEvent; // Menggunakan java.sql.Date untuk interaksi dengan database
    protected String lokasi;

    // Konstruktor
    public Event(int id, String namaEvent, java.sql.Date tanggalEvent, String lokasi) {
        this.id = id;
        this.namaEvent = namaEvent;
        this.tanggalEvent = tanggalEvent;
        this.lokasi = lokasi;
    }

    // Metode untuk format tanggal
    public String getTanggalFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(tanggalEvent);
    }

    // Metode untuk manipulasi string
    public String getNamaEventUpperCase() {
        return namaEvent.toUpperCase();
    }
}

// Kelas turunannya Event Musik
class MusicEvent extends Event implements DetailEvent {
    private String pengisiAcara;

    // Konstruktor
    public MusicEvent(int id, String namaEvent, java.sql.Date tanggalEvent, String lokasi, String pengisiAcara) {
        super(id, namaEvent, tanggalEvent, lokasi);
        this.pengisiAcara = pengisiAcara;
    }

    @Override
    public void tampilkanDetail() {
        System.out.println("=== Detail Event Musik ===");
        System.out.println("ID: " + id);
        System.out.println("Nama Event: " + namaEvent);
        System.out.println("Tanggal Event: " + getTanggalFormatted());
        System.out.println("Lokasi: " + lokasi);
        System.out.println("Pengisi Acara: " + pengisiAcara);
    }
}

// Kelas utama untuk mengelola event
public class MusicEventManager {
    private static Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/music_event_db";
        String user = "root";
        String password = ""; // Password default XAMPP
        return DriverManager.getConnection(url, user, password);
    }

    public static void tambahEvent(int id, String namaEvent, String dateTime, String lokasi, String pengisiAcara) {
        String query = "INSERT INTO events (id, name, date, location, pengisi_acara) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(dateTime); // Timestamp untuk tanggal dan waktu
            stmt.setInt(1, id);
            stmt.setString(2, namaEvent);
            stmt.setTimestamp(3, timestamp);
            stmt.setString(4, lokasi);
            stmt.setString(5, pengisiAcara);
            stmt.executeUpdate();
            System.out.println("Event berhasil ditambahkan.");
        } catch (SQLException e) {
            System.out.println("Error (SQL): " + e.getMessage());
        }
    }

    public static void bacaEvent() {
        String query = "SELECT * FROM events";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("=== Data Event ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Nama: " + rs.getString("name")
                        + ", Tanggal: " + rs.getTimestamp("date") + ", Lokasi: " + rs.getString("location")
                        + ", Pengisi Acara: " + rs.getString("pengisi_acara"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void perbaruiEvent(int id, String namaBaru, String newDateTime, String newLokasi, String newPengisiAcara) {
        String query = "UPDATE events SET name = ?, date = ?, location = ?, pengisi_acara = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(newDateTime); // Timestamp untuk tanggal dan waktu
            stmt.setString(1, namaBaru);
            stmt.setTimestamp(2, timestamp);
            stmt.setString(3, newLokasi);
            stmt.setString(4, newPengisiAcara);
            stmt.setInt(5, id);
            stmt.executeUpdate();
            System.out.println("Event berhasil diperbarui.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void hapusEvent(int id) {
        String query = "DELETE FROM events WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Event berhasil dihapus.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Fungsi untuk menampilkan statistik event berdasarkan pengisi acara
    public static void statistikEventBerdasarkanPengisiAcara() {
        String query = "SELECT pengisi_acara, COUNT(*) AS total_events FROM events GROUP BY pengisi_acara";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("=== Statistik Event Berdasarkan Pengisi Acara ===");
            while (rs.next()) {
                String pengisiAcara = rs.getString("pengisi_acara");
                int totalEvents = rs.getInt("total_events");
                System.out.println("Pengisi Acara: " + pengisiAcara + ", Total Event: " + totalEvents);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Fungsi untuk menampilkan statistik event berdasarkan lokasi
    public static void statistikEventBerdasarkanLokasi() {
        String query = "SELECT location, COUNT(*) AS total_events FROM events GROUP BY location";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("=== Statistik Event Berdasarkan Lokasi ===");
            while (rs.next()) {
                String location = rs.getString("location");
                int totalEvents = rs.getInt("total_events");
                System.out.println("Lokasi: " + location + ", Total Event: " + totalEvents);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Fungsi untuk menghitung perbedaan tanggal
    public static void hitungPerbedaanTanggal() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan ID Event untuk menghitung perbedaan hari: ");
        int eventId = scanner.nextInt();
        scanner.nextLine();  // Konsumsi newline
        
        String query = "SELECT date FROM events WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                java.sql.Date tanggalEvent = rs.getDate("date");  // Menggunakan java.sql.Date
                LocalDate eventLocalDate = tanggalEvent.toLocalDate(); // Mengubah menjadi LocalDate
                LocalDate currentDate = LocalDate.now();
                long daysBetween = ChronoUnit.DAYS.between(eventLocalDate, currentDate);
                System.out.println("Perbedaan hari antara event dan hari ini: " + daysBetween + " hari.");
            } else {
                System.out.println("Event dengan ID tersebut tidak ditemukan.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean login(Scanner scanner) {
        String username = "admin";
        String password = "admin123";
        boolean authenticated = false;

        System.out.print("Masukkan Username: ");
        String inputUsername = scanner.nextLine();

        System.out.print("Masukkan Password: ");
        String inputPassword = scanner.nextLine();

        int captcha = ThreadLocalRandom.current().nextInt(1000, 9999);
        System.out.println("Captcha: " + captcha);
        System.out.print("Masukkan Captcha: ");
        int inputCaptcha = scanner.nextInt();
        scanner.nextLine(); // Konsumsi newline

        if (inputUsername.equals(username) && inputPassword.equals(password) && inputCaptcha == captcha) {
            authenticated = true;
            System.out.println("Login berhasil! Selamat datang, Admin.");
        } else {
            System.out.println("Login gagal! Silakan coba lagi.");
        }

        return authenticated;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        if (!login(scanner)) {
            System.out.println("Akses ditolak.");
            return;
        }

        while (true) {
            System.out.println("\n=== Menu ===");
            System.out.println("1. Tambah Event");
            System.out.println("2. Lihat Semua Event");
            System.out.println("3. Perbarui Event");
            System.out.println("4. Hapus Event");
            System.out.println("5. Hitung Perbedaan Hari");
            System.out.println("6. Statistik Event Berdasarkan Pengisi Acara");
            System.out.println("7. Statistik Event Berdasarkan Lokasi");
            System.out.println("8. Keluar");
            System.out.print("Pilih opsi: ");
            int pilihan = scanner.nextInt();
            scanner.nextLine(); // Konsumsi newline

            try {
                switch (pilihan) {
                    case 1:
                        System.out.print("Masukkan ID Event: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Masukkan Nama Event: ");
                        String name = scanner.nextLine();
                        System.out.print("Masukkan Tanggal dan Waktu Event (YYYY-MM-DD HH:MM:SS): ");
                        String dateTime = scanner.nextLine();
                        System.out.print("Masukkan Lokasi Event: ");
                        String location = scanner.nextLine();
                        System.out.print("Masukkan Pengisi Acara: ");
                        String pengisiAcara = scanner.nextLine();
                        tambahEvent(id, name, dateTime, location, pengisiAcara);
                        break;

                    case 2:
                        bacaEvent();
                        break;

                    case 3:
                        System.out.print("Masukkan ID Event yang akan diperbarui: ");
                        int updateId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Masukkan Nama Baru: ");
                        String newName = scanner.nextLine();
                        System.out.print("Masukkan Tanggal dan Waktu Baru (YYYY-MM-DD HH:MM:SS): ");
                        String newDateTime = scanner.nextLine();
                        System.out.print("Masukkan Lokasi Baru: ");
                        String newLocation = scanner.nextLine();
                        System.out.print("Masukkan Pengisi Acara Baru: ");
                        String newPengisiAcara = scanner.nextLine();
                        perbaruiEvent(updateId, newName, newDateTime, newLocation, newPengisiAcara);
                        break;

                    case 4:
                        System.out.print("Masukkan ID Event yang akan dihapus: ");
                        int deleteId = scanner.nextInt();
                        hapusEvent(deleteId);
                        break;

                    case 5:
                        hitungPerbedaanTanggal();
                        break;

                    case 6:
                        statistikEventBerdasarkanPengisiAcara();
                        break;

                    case 7:
                        statistikEventBerdasarkanLokasi();
                        break;

                    case 8:
                        System.out.println("Keluar dari program...");
                        scanner.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Pilihan tidak valid!");
                }
            } catch (Exception e) {
                System.out.println("Terjadi kesalahan: " + e.getMessage());
            }
        }
    }
}
