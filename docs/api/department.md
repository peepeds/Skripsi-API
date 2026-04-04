# Dokumentasi API — Departemen

Base path: `/department`

---

## GET /department

**Autentikasi**: Publik

Mengambil daftar seluruh departemen.

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Get All Department",
  "result": [
    {
      "id": 1,
      "deptName": "Teknik",
      "active": true
    },
    {
      "id": 2,
      "deptName": "Bisnis",
      "active": true
    }
  ]
}
```

---

## POST /department

**Autentikasi**: Hanya ADMIN

Menambahkan departemen baru.

### Request Body

| Field    | Tipe   | Wajib | Validasi                       |
|----------|--------|-------|--------------------------------|
| deptName | string | Ya    | Tidak boleh kosong, minimal 3 karakter |

### Contoh Request Body

```json
{
  "deptName": "Ilmu Komputer"
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Create Department",
  "result": {
    "id": 3,
    "deptName": "Ilmu Komputer",
    "active": true
  }
}
```

---

## PATCH /department/{id}

**Autentikasi**: Hanya ADMIN

Memperbarui data departemen berdasarkan ID.

### Path Parameters

| Parameter | Tipe    | Keterangan     |
|-----------|---------|----------------|
| id        | integer | ID departemen  |

### Request Body

| Field    | Tipe    | Wajib | Validasi             |
|----------|---------|-------|----------------------|
| deptName | string  | Tidak | Minimal 3 karakter   |
| active   | boolean | Tidak | —                    |

### Contoh Request Body

```json
{
  "deptName": "Ilmu Komputer dan Rekayasa",
  "active": true
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Update Department",
  "result": {
    "id": 3,
    "deptName": "Ilmu Komputer dan Rekayasa",
    "active": true
  }
}
```
