# Dokumentasi API — Autentikasi

Base path: `/auth`

---

## POST /auth/register

**Autentikasi**: Publik

Mendaftarkan akun pengguna baru.

### Request Body

| Field        | Tipe    | Wajib | Validasi                                               |
|--------------|---------|-------|--------------------------------------------------------|
| firstName    | string  | Ya    | Tidak boleh kosong                                     |
| lastName     | string  | Tidak | —                                                      |
| email        | string  | Ya    | Format email valid                                     |
| password     | string  | Ya    | Harus memenuhi aturan `@ValidPassword`                 |
| regionId     | integer | Ya    | Tidak boleh null                                       |
| majorId      | integer | Ya    | Tidak boleh null                                       |
| registerId   | string  | Ya    | Panjang 5–10 karakter, format `@ValidRegisterId`       |
| phoneNumber  | string  | Ya    | Format `@ValidPhoneNumber`                             |

### Contoh Request Body

```json
{
  "firstName": "Budi",
  "lastName": "Santoso",
  "email": "budi@mahasiswa.ac.id",
  "password": "P@ssw0rd123",
  "regionId": 1,
  "majorId": 3,
  "registerId": "2021001",
  "phoneNumber": "081234567890"
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Register success",
  "result": "Successfully created account"
}
```

---

## POST /auth/login

**Autentikasi**: Publik

Melakukan login dan mengembalikan `accessToken`. Cookie `refreshToken` (HttpOnly, Secure) akan otomatis di-set pada response.

### Request Body

| Field    | Tipe   | Wajib | Validasi           |
|----------|--------|-------|--------------------|
| email    | string | Ya    | Tidak boleh kosong |
| password | string | Ya    | Tidak boleh kosong |

### Contoh Request Body

```json
{
  "email": "budi@mahasiswa.ac.id",
  "password": "P@ssw0rd123"
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Login success",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

> **Catatan**: Response juga menyertakan header `Set-Cookie` dengan nilai `refreshToken` yang bersifat HttpOnly dan Secure.

---

## POST /auth/refresh

**Autentikasi**: Publik (membaca cookie `refreshToken`)

Memperbarui `accessToken` menggunakan `refreshToken` yang tersimpan di cookie. Tidak memerlukan request body.

### Contoh Response

```json
{
  "success": true,
  "message": "New access token created",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### Contoh Response Error (tanpa cookie)

```json
{
  "success": false,
  "message": "Refresh token is missing"
}
```

---

## POST /auth/logout

**Autentikasi**: Publik (membaca cookie `refreshToken`)

Menginvalidasi `refreshToken` dan menghapus cookie. Tidak memerlukan request body.

### Contoh Response

```json
{
  "success": true,
  "message": "Logout success"
}
```
