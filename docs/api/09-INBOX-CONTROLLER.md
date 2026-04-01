# Inbox Controller Documentation

Base URL: `/inbox`

---

## 1. Get Inbox Preview

**Endpoint:** `GET /inbox`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengambil preview pesan inbox pengguna dengan pagination.

### Query Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| cursor | Long | Tidak | - | Cursor untuk pagination |
| limit | Integer | Tidak | 5 | Jumlah data per halaman |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Inbox fetched",
  "result": [
    {
      "id": 1,
      "subject": "Aplikasi Anda Telah Diproses",
      "sender": "admin@example.com",
      "preview": "Kami telah meninjau aplikasi Anda...",
      "isRead": false,
      "createdAt": "2024-02-01T10:30:00Z"
    },
    {
      "id": 2,
      "subject": "Review Sertifikat Anda Disetujui",
      "sender": "admin@example.com",
      "preview": "Sertifikat Anda telah diverifikasi dan disetujui...",
      "isRead": true,
      "createdAt": "2024-01-28T15:45:00Z"
    }
  ],
  "meta": {
    "nextCursor": 3,
    "hasMore": true,
    "limit": 5
  }
}
```

### Error Response (401 Unauthorized)

```json
{
  "success": false,
  "message": "Unauthorized"
}
```

---

## Response Format

Semua endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": array,
  "meta": object (optional)
}
```

## Authentication

- **Get Inbox Preview:** Requires authentication

## Notes

- Setiap pesan dapat dibuka untuk melihat detail lengkap
- Limit default adalah 5 pesan (dapat disesuaikan)
- Pagination menggunakan cursor untuk efisiensi
- Pesan ditampilkan dari yang terbaru ke yang terlama
