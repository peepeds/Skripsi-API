# Category Controller Documentation

---

## 1. Get Categories

**Endpoint:** `GET /category`

**Access:** Public

**Deskripsi:** Mengambil daftar kategori dengan opsi untuk menyertakan sub-kategori.

### Query Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| IncludeSubCategories | Integer | Tidak | 1 | 1 untuk include, 0 untuk exclude |
| type | String | Tidak | jobs | Tipe kategori (jobs/companies) |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Categories data",
  "result": [
    {
      "id": 1,
      "name": "IT & Software",
      "description": "Kategori teknologi informasi",
      "icon": "https://minio.example.com/it-icon.png",
      "subCategories": [
        {
          "id": 10,
          "name": "Backend Developer",
          "description": "Developer backend"
        },
        {
          "id": 11,
          "name": "Frontend Developer",
          "description": "Developer frontend"
        }
      ]
    }
  ]
}
```

### Success Response - Without SubCategories (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Categories data",
  "result": [
    {
      "id": 1,
      "name": "IT & Software",
      "description": "Kategori teknologi informasi",
      "icon": "https://minio.example.com/it-icon.png"
    }
  ]
}
```

---

## 2. Get Companies by SubCategory

**Endpoint:** `GET /subcategory/{subCategoryName}/companies`

**Access:** Public

**Deskripsi:** Mengambil daftar perusahaan berdasarkan sub-kategori dengan pagination.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| subCategoryName | String | Nama sub-kategori |

### Query Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| type | String | Tidak | companies | Tipe data yang diambil |
| cursor | Long | Tidak | - | Cursor untuk pagination |
| limit | Integer | Tidak | 10 | Jumlah data per halaman |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Companies by SubCategory data",
  "result": [
    {
      "id": 1,
      "name": "TechCorp Indonesia",
      "slug": "techcorp-indonesia",
      "logo": "https://minio.example.com/logo.png",
      "description": "Perusahaan teknologi terdepan",
      "rating": 4.5,
      "reviewCount": 120
    }
  ],
  "meta": {
    "nextCursor": 2,
    "hasMore": true,
    "limit": 10
  }
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "SubCategory not found"
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

- **Get Categories:** Public
- **Get Companies by SubCategory:** Public
