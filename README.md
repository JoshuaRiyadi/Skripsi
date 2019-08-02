# WSN-RBS 

# Profil 

Nama : Joshua Riyadi

Skripsi : Pengembangan Sinkronisasi Waktu di Wireless Sensor Network dengan Algoritma Reference Broadcast Synchronization 

Dosen Pembimbing : Elisati Hulu, M.T

# Deskripsi 

1. Membangun aplikasi sinkronisasi waktu pada WSN dengan menggunakan algoritma Reference
Broadcast Synchronization.
2. Melakukan perbandingan kinerja antara algoritma Reference Broadcast Synchronization dengan
algoritma Flooding Time Synchronization Protocol.

# Software Requirements 

1. Eclipse IDE
2. Java JDK $ JRE
3. Apache Ant

# Hardware Requirement

1. Node sensor Preon32 atau Preon32 Shuttle
2. Kabel USB Tipe A
3. Antarmuka (laptop/komputer) sebanyak daerah persebaran yang diperlukan

# Keterangan Git

1. Folder 'CommandLine'
    'CommandLine' berisi kode program yang digunakan untuk membuat jar pada antarmuka. Kode utama terdapat pada "src/connectionBeacon.java" dan  "src/connectionBaseStation.java"
2. Folder 'Sandbox'
    'Sandbox' berisi kode-kode program yang akan diunggah pada node sensor. Kode utama terdapat pada "src/BaseStation.java" , "src/Node.java" ,        dan "src/Beacon.java" 

# Cara Menjalankan 


    1. Unggah kode program pada node sensor root :
    a. Buka context1.properties pada folder config.
    b. Sesuaikan nilai comport dengan nomor port node sensor (dapat dilihat pada "Device Manager -> Ports (COM & LPT)").
    c. Pilih menu context.set.1 pada Apache Ant bagian Preon32 Sandbox User untuk masuk pada konteks tersebut.
    d. Pilih menu .all pada Apache Ant bagian Preon32 Sandbox untuk mengunggah kode program BaseStation pada node sensor.
    2. Unggah kode program pada node sensor biasa :
    a. Buka context2.properties atau # seterusnya pada config.
    b. Sesuaikan nilai comport dengan nomor port node sensor.
    c. Pilih menu context.set.2 atau # seterusnya pada Apache Ant bagian Preon32 Sandbox User untuk masuk pada konteks tersebut.
    d. Pilih menu .all pada Apache Ant bagian Preon32 Sandbox untuk mengunggah kode program SensorNode pada node sensor.
    3. Sesuaikan nomor port node sensor root pada connectionBaseStation.java (line 109).
    4. Klik kanan pada project Sandbox dan pilih menu Export.
    5. Pilih "Java -> Runnable JAR file".
    6. Pilih "CMD - CommandLine" pada menu Launch configuration.
    7. Tempatkan hasil jar pada folder yang diinginkan melalui menu Export destination.
    8. Setelah jar terbentuk, perangkat lunak dapat dijalankan melalui command prompt.
    
# Fitur 


    Memulai pengiriman pesan sinkronisasi waktu sebanyak 10 pesan sinkronisasi.
    Perintah untuk memanggil fitur : 1
    Meminta nilai waktu yang dimiliki oleh sensor node
    Perintah untuk memanggil fitur : 2
    Keluar dari program dan mematikan Base Station.
    Perintah untuk memanggil fitur : 3


