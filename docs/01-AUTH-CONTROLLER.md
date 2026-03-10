# Auth Controller Documentation

Base URL: `/auth`

---

## 1. Register Account

**Endpoint:** `POST /auth/register`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Membuat akun pengguna baru di sistem.

### Request Body

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "regionId": 1,
  "majorId": 5,
  "registerId": "D12345",
  "phoneNumber": "081234567890"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| firstName | String | Ya | Nama depan pengguna (minimal 1 karakter) |
| lastName | String | Tidak | Nama belakang pengguna |
| email | String | Ya | Email yang belum digunakan (format valid) |
| password | String | Ya | Password kuat (minimal 8 karakter, huruf besar, angka, simbol) |
| regionId | Integer | Ya | ID region/kampus pengguna |
| majorId | Integer | Ya | ID jurusan pengguna |
| registerId | String | Ya | Nomor induk mahasiswa (5-10 karakter) |
| phoneNumber | String | Ya | Nomor telepon (format: 08xxxxxxxxxx) |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Register success",
  "result": "Successfully Created Account"
}
```

### Error Response

- **400 Bad Request**: Data tidak valid (email sudah terdaftar, password lemah, dll)
- **409 Conflict**: Email sudah digunakan

---

## 2. Login

**Endpoint:** `POST /auth/login`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Login dengan email dan password, mendapatkan access token dan refresh token (disimpan di cookie).

### Request Body

```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| email | String | Ya | Email akun yang terdaftar |
| password | String | Ya | Password akun |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Login success",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Header Response:**
```
Set-Cookie: refreshToken=<refresh_token>; HttpOnly; Secure; Path=/auth; SameSite=None; MaxAge=<duration>
```

### Error Response

- **400 Bad Request**: Email atau password tidak sesuai
- **401 Unauthorized**: Kredensial tidak valid

---

## 3. Refresh Access Token

**Endpoint:** `POST /auth/refresh`

**Access:** Publik (tapi memerlukan refresh token di cookie)

**Deskripsi:** Membuat access token baru menggunakan refresh token yang tersimpan di cookie.

### Request

Tidak ada request body, hanya kirim request dengan cookie yang berisi `refreshToken`.

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Cookie: refreshToken=<refresh_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "New access token created",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### Error Response

- **400 Bad Request**: Refresh token tidak ditemukan atau tidak valid
- **401 Unauthorized**: Refresh token sudah kadaluarsa atau di-revoke

---

## 4. Logout

**Endpoint:** `POST /auth/logout`

**Access:** Publik (tapi memerlukan refresh token di cookie)

**Deskripsi:** Logout dan revoke refresh token, menghapus cookie refreshToken.

### Request

Tidak ada request body, hanya kirim request dengan cookie yang berisi `refreshToken`.

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Cookie: refreshToken=<refresh_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Logout success"
}
```

**Header Response:**
```
Set-Cookie: refreshToken=; HttpOnly; Secure; Path=/auth; MaxAge=0
```

### Notes

- Refresh token akan di-revoke dari database
- Cookie `refreshToken` akan dihapus dari browser

---

## Important Notes

- **Access Token**: Gunakan di header `Authorization: Bearer <accessToken>` untuk request yang memerlukan autentikasi
- **Refresh Token**: Disimpan otomatis di cookie, dikirim otomatis oleh browser
- **Token Expiration**: Access token biasanya berlaku 15 menit, refresh token berlaku lebih lama
- **Security**: Semua password harus kuat (minimal 8 karakter, huruf besar, angka, simbol)

