# Dokumentasi API — Inbox

Base path: `/inbox`

---

## GET /inbox

**Autentikasi**: Harus login

Mengambil pratinjau daftar pesan/notifikasi masuk milik pengguna yang sedang login dengan cursor-based pagination.

### Query Parameters

| Parameter | Tipe    | Wajib | Default | Keterangan                                  |
|-----------|---------|-------|---------|---------------------------------------------|
| cursor    | long    | Tidak | —       | ID terakhir dari halaman sebelumnya         |
| limit     | integer | Tidak | 5       | Jumlah pesan per halaman                    |

### Contoh Response

```json
{
  "success": true,
  "message": "Inbox fetched",
  "meta": {
    "nextCursor": 88,
    "hasMore": true
  },
  "result": [
    {
      "id": 95,
      "title": "Pengajuan sertifikat disetujui",
      "body": "Sertifikat 'Belajar Machine Learning' Anda telah disetujui.",
      "isRead": false,
      "createdAt": "2026-04-02T11:00:00"
    },
    {
      "id": 90,
      "title": "Pengajuan perusahaan diproses",
      "body": "Pengajuan perusahaan 'PT Inovasi Digital' sedang ditinjau.",
      "isRead": true,
      "createdAt": "2026-04-01T08:30:00"
    }
  ]
}
```
