# Dokumentasi API — Program Studi (Major)

Base path: `/major`

---

## GET /major

**Autentikasi**: Publik

Mengambil daftar seluruh program studi.

### Contoh Response

```json
{
  "success": true,
  "message": "successfully Get All Major",
  "result": [
    {
      "id": 1,
      "majorName": "Teknik Informatika",
      "regionId": 1,
      "deptId": 1,
      "active": true
    },
    {
      "id": 2,
      "majorName": "Sistem Informasi",
      "regionId": 1,
      "deptId": 1,
      "active": true
    }
  ]
}
```

---

## POST /major

**Autentikasi**: Hanya ADMIN

Menambahkan program studi baru.

### Request Body

| Field     | Tipe    | Wajib | Validasi           |
|-----------|---------|-------|--------------------|
| majorName | string  | Ya    | Tidak boleh kosong |
| deptId    | integer | Ya    | Tidak boleh null   |
| regionId  | integer | Ya    | Tidak boleh null   |

### Contoh Request Body

```json
{
  "majorName": "Teknik Komputer",
  "deptId": 1,
  "regionId": 2
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Created New Major",
  "result": {
    "id": 3,
    "majorName": "Teknik Komputer",
    "regionId": 2,
    "deptId": 1,
    "active": true
  }
}
```

---

## PATCH /major/{id}

**Autentikasi**: Hanya ADMIN

Memperbarui data program studi berdasarkan ID.

### Path Parameters

| Parameter | Tipe    | Keterangan         |
|-----------|---------|--------------------|
| id        | integer | ID program studi   |

### Request Body

| Field     | Tipe    | Wajib | Validasi             |
|-----------|---------|-------|----------------------|
| majorName | string  | Tidak | Minimal 5 karakter   |
| regionId  | integer | Tidak | —                    |
| deptId    | integer | Tidak | —                    |
| active    | boolean | Tidak | —                    |

### Contoh Request Body

```json
{
  "majorName": "Rekayasa Perangkat Lunak",
  "active": true
}
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Updated Major",
  "result": {
    "id": 3,
    "majorName": "Rekayasa Perangkat Lunak",
    "regionId": 2,
    "deptId": 1,
    "active": true
  }
}
```

---

## GET /major/options

**Autentikasi**: Publik

Mengambil daftar program studi dalam format ringkas untuk digunakan sebagai opsi pada formulir (dropdown).

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully Get Major",
  "result": [
    {
      "id": 1,
      "majorName": "Teknik Informatika"
    },
    {
      "id": 2,
      "majorName": "Sistem Informasi"
    }
  ]
}
```
