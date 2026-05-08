# Android Uygulama Geliştirme Prompt (Shizuku & GUI)

Amaç: Shizuku'dan erişim alıp hotspot'a bağlı cihazları listeleyen, Material tema ve ayarlar paneli olan Android uygulaması.

---

## 1. Ana Özellikler

Uygulama açıldığında Shizuku erişimi iste.

ARP tablosunu okuyup bağlı cihazların IP ve MAC adreslerini listele.

Material Design teması (Light & Dark mod destekli).

Liste ekranında:
- IP Adresi
- MAC Adresi
- Bağlantı durumu (opsiyonel)

Ayarlarda:
- Tema değiştirme (Light/Dark/System Default)
- Shizuku bağlantısını yeniden isteme/yenileme

---

## 2. Uygulama Akışı

### 1. Splash / Başlangıç:
- Shizuku izin isteği.
- Eğer izin verilmezse bilgilendirme ve yeniden deneme seçeneği.

### 2. Ana Liste Ekranı:
- Başlık: "Hotspot Bağlı Cihazlar"
- Liste: RecyclerView veya Jetpack Compose LazyColumn
- Swipe refresh ile yenileme
- Eğer bağlı cihaz yoksa "Bağlı cihaz bulunamadı" mesajı

### 3. Ayarlar Ekranı:
- Tema: Light / Dark / System
- Shizuku erişimi: Tekrar iste

---

## 3. Shizuku Entegrasyonu (Kotlin Örneği)

```kotlin
// Shizuku ile izin isteme
if (!Shizuku.pingBinder()) {
    Shizuku.requestPermission(0) // 0 = request code
}

// ARP tablosunu oku
fun getConnectedDevices(): List<Device> {
    val devices = mutableListOf<Device>()
    val file = File("/proc/net/arp")
    file.forEachLine { line ->
        val parts = line.split("\\s+".toRegex())
        if (parts.size >= 6 && parts[0] != "IP") {
            devices.add(Device(ip = parts[0], mac = parts[3]))
        }
    }
    return devices
}

data class Device(val ip: String, val mac: String)
```

---

## 4. GUI Tasarımı (Material Design, Compose Örneği)

```kotlin
@Composable
fun DeviceListScreen(devices: List<Device>, onRefresh: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Hotspot Bağlı Cihazlar") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Yenile")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            if (devices.isEmpty()) {
                item {
                    Text(
                        "Bağlı cihaz bulunamadı",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(devices) { device ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("IP: ${device.ip}")
                            Text("MAC: ${device.mac}")
                        }
                    }
                }
            }
        }
    }
}
```

---

## 5. Ayarlar Ekranı

```kotlin
@Composable
fun SettingsScreen(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit, onShizukuRetry: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tema Ayarları", style = MaterialTheme.typography.h6)
        Row {
            RadioButton(selected = themeMode == ThemeMode.LIGHT, onClick = { onThemeChange(ThemeMode.LIGHT) })
            Text("Light")
            RadioButton(selected = themeMode == ThemeMode.DARK, onClick = { onThemeChange(ThemeMode.DARK) })
            Text("Dark")
            RadioButton(selected = themeMode == ThemeMode.SYSTEM, onClick = { onThemeChange(ThemeMode.SYSTEM) })
            Text("System Default")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onShizukuRetry) { Text("Shizuku İzin Yenile") }
    }
}
```

---

## 6. Öneriler / Geliştirmeler

- Listeyi otomatik yenile (10-15 saniye aralıklarla).
- Bağlı cihazları renk ile durumlandır (aktif/pasif).
- İleri seviye: IP'ye göre cihaz isimlerini çözümle (reverse DNS).

---

## 7. Teknik Gereksinimler

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Build Variants**:
  - Debug (unsigned)
  - Release (signed with keystore)

---

## 8. Gerekli Dependencies

```gradle
// Shizuku
implementation("dev.rikka.shizuku:api:13")

// Jetpack Compose
implementation(platform('androidx.compose:compose-bom:2024.02.00'))
implementation('androidx.compose.ui:ui')
implementation('androidx.compose.material3:material3')

// Navigation
implementation('androidx.navigation:navigation-compose:2.7.7')

// Hilt
implementation('com.google.dagger:hilt-android:2.50')
kapt('com.google.dagger:hilt-android-compiler:2.50')

// Coroutines
implementation('org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3')

// DataStore (Preferences)
implementation('androidx.datastore:datastore-preferences:1.0.0')
```

---

Bu prompt'u kullanarak Android Studio'da Kotlin + Jetpack Compose projesi oluşturabilirsiniz.