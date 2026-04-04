# Dokumentasi API — Region (Wilayah)

Base path: `/region`

---

## GET /region

**Autentikasi**: Publik

Mengambil daftar seluruh wilayah.

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Get All Regions Data",
  "result": [
    {
      "id": 1,
      "regionName": "Jawa Barat",
      "active": true
    },
    {
      "id": 2,
      "regionName": "DKI Jakarta",
      "active": true
    }
  ]
}
```

---

## POST /region

**Autentikasi**: Hanya ADMIN

Menambahkan wilayah baru.

### Request Body

| Field      | Tipe   | Wajib | Validasi                |
|------------|--------|-------|-------------------------|
| regionName | string | Ya    | Tidak boleh kosong, minimal 5 karakter |

### Contoh Request Body

```json
{
  "regionName": "Jawa Tengah"
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Created new Region",
  "result": {
    "id": 3,
    "regionName": "Jawa Tengah",
    "active": true
  }
}
```

---

## PATCH /region/{id}

**Autentikasi**: Hanya ADMIN

Memperbarui data wilayah berdasarkan ID.

### Path Parameters

| Parameter | Tipe    | Keterangan    |
|-----------|---------|---------------|
| id        | integer | ID wilayah    |

### Request Body

| Field      | Tipe    | Wajib | Validasi |
|------------|---------|-------|----------|
| regionName | string  | Tidak | —        |
| active     | boolean | Tidak | —        |

### Contoh Request Body

```json
{
  "regionName": "Jawa Tengah & DIY",
  "active": true
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Updated Region",
  "result": {
    "id": 3,
    "regionName": "Jawa Tengah & DIY",
    "active": true
  }
}
```

---

## GET /region/options

**Autentikasi**: Publik

Mengambil daftar wilayah dalam format ringkas untuk digunakan sebagai opsi pada formulir (dropdown).

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Get Regions",
  "result": [
    {
      "id": 1,
      "regionName": "Jawa Barat"
    },
    {
      "id": 2,
      "regionName": "DKI Jakarta"
    }
  ]
}
```
