# Test Controller Documentation

Base URL: `/`

---

## 1. Ping / Health Check

**Endpoint:** `GET /ping`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Endpoint sederhana untuk mengecek apakah server API sedang berjalan dengan baik (health check).

### Request

```bash
curl -X GET "http://localhost:8080/ping"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "pong",
  "result": "ping pong"
}
```

---

## Use Cases

- **Server Health Check**: Gunakan endpoint ini untuk memastikan server API sedang running
- **Connection Test**: Test koneksi ke API sebelum melakukan request yang lebih kompleks
- **Monitoring**: Dapat diintegrasikan dengan monitoring tools untuk tracking uptime server
- **Development**: Sangat berguna saat development untuk quick testing

---

## Notes

- Endpoint ini tidak memerlukan autentikasi
- Response selalu sukses jika server berjalan
- Sangat cepat dan lightweight untuk health check

