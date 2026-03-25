# Inbox Controller Documentation

Base URL: `/inbox`

---

## Overview

Inbox adalah sistem notifikasi terpusat berbasis **Polymorphic Association**. Setiap aktivitas request (apapun tipe-nya) akan membuat sebuah `InboxEntry` yang terhubung ke entitas aslinya via `requestType` + `referenceId`.

Untuk melihat detail request, gunakan `referenceUrl` yang sudah disediakan langsung di response, atau gunakan endpoint spesifik per tipe:
- `COMPANY_REQUEST` → `GET /company/request/{referenceId}`

---

## 1. Get User Inbox

**Endpoint:** `GET /inbox`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mendapatkan daftar notifikasi milik user yang sedang login, diurutkan dari yang paling baru.

### Request Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| page | Integer | Tidak | 0 | Nomor halaman (dimulai dari 0) |
| limit | Integer | Tidak | 5 | Jumlah data per halaman |

### Example Request

```bash
curl -X GET "http://localhost:8080/inbox?page=0&limit=5" \
  -H "Authorization: Bearer <access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Inbox fetched",
  "meta": {
    "page": 0,
    "size": 5,
    "totalElements": 2,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  },
  "result": [
    {
      "inboxId": 2,
      "activity": "Rejected Company: PT Ayam Goreng",
      "requestType": "COMPANY_REQUEST",
      "referenceId": 2,
      "referenceUrl": "/company/request/2",
      "status": "REJECTED",
      "isRead": false,
      "createdAt": "2026-02-28T03:52:40Z"
    },
    {
      "inboxId": 1,
      "activity": "Approved Company: PT Hidup Internasional",
      "requestType": "COMPANY_REQUEST",
      "referenceId": 1,
      "referenceUrl": "/company/request/1",
      "status": "APPROVED",
      "isRead": false,
      "createdAt": "2026-02-28T03:50:57Z"
    }
  ]
}
```

### Response Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| inboxId | Long | ID dari inbox entry |
| activity | String | Deskripsi aktivitas notifikasi, mencerminkan status terkini (misal: `Approved Company: ...`, `Rejected Company: ...`) |
| requestType | String | Tipe request: `COMPANY_REQUEST`, dll |
| referenceId | Long | ID dari entitas terkait |
| referenceUrl | String | URL langsung ke detail entitas terkait. `null` jika tipe belum terdaftar |
| status | String | Status: `PENDING`, `APPROVED`, `REJECTED`, `CANCELED` |
| isRead | Boolean | Sudah dibaca atau belum |
| createdAt | DateTime | Waktu notifikasi dibuat |

### Error Response

- **401 Unauthorized**: Tidak terautentikasi

---

## Navigasi ke Detail

`referenceUrl` sudah disediakan langsung di tiap item response. Mapping URL dikelola secara dinamis di `InboxService.REFERENCE_URL_TEMPLATES` — untuk menambah tipe baru cukup tambahkan satu entry di map tersebut.

| requestType | referenceUrl |
|-------------|--------------|
| `COMPANY_REQUEST` | `/company/request/{referenceId}` |
| _(tipe lain)_ | Daftarkan di `REFERENCE_URL_TEMPLATES` pada `InboxService` |

---

## Important Notes

- **Terpusat**: Semua tipe request masuk ke inbox yang sama — tidak ada inbox per-modul
- **Polymorphic**: `requestType` + `referenceId` mengidentifikasi entitas asli tanpa foreign key langsung
- **activity**: Nama field sebelumnya `title`, diubah menjadi `activity` dan bersifat deskriptif sesuai status (bukan hanya `Add Company: ...`)
- **referenceUrl**: Di-generate otomatis di backend berdasarkan `requestType`. Client tidak perlu construct URL sendiri
- **isRead**: Field ini tersedia untuk tracking notif yang sudah/belum dibaca oleh client
- **Default Limit**: Default limit di Inbox adalah **5**
