📁 file-management-service

A Spring Boot service for uploading, validating, converting, storing, indexing, and retrieving structured files.
Currently supports XML → JSON transformation with fast indexed search by customer, type, and date.

🚀 Features

Upload XML files with strict filename validation

Convert XML to JSON

Store JSON files in a structured filesystem

Prevent duplicate uploads

Update existing files

Delete files and their index references

Retrieve file content by name

Indexed search by:

customer

type

date

Indexes are implemented using symbolic links, enabling fast lookups.

🧩 Business Processes
1. File Upload

Validate filename (<customer>_<type>_<yyyy-MM-dd>.xml)

Parse XML and convert to JSON

Save JSON under:

storage/files/<customer>_<type>_<yyyy-MM-dd>.json


Create index links:

storage/index-by-customer/<customer>/<file>.json → ../../files/<file>.json
storage/index-by-type/<type>/<file>.json → ../../files/<file>.json
storage/index-by-date/<yyyy-MM-dd>/<file>.json → ../../files/<file>.json


Return 201 Created with downloadable resource

2. File Update

Same as upload, but existing file is replaced

Index links are recreated

Returns 202 Accepted

3. File Delete

Remove main JSON file

Remove all symlink indexes:

index-by-customer/
index-by-type/
index-by-date/


Return 204 No Content

4. File Retrieval

Return file content by original XML filename

Convert internal .json back to external .xml-styled naming when presenting results

5. Indexed Search
By Date

Read directory:

storage/index-by-date/<yyyy-MM-dd>/

By Customer
storage/index-by-customer/<customer>/

By Type
storage/index-by-type/<type>/


All lookups are O(1) directory reads.

📦 Storage Structure
storage/
  files/
    acme_report_2025-12-09.json
    globex_summary_2025-12-10.json

  index-by-customer/
    acme/
      acme_report_2025-12-09.json → ../../files/acme_report_2025-12-09.json

  index-by-type/
    report/
      acme_report_2025-12-09.json → ../../files/acme_report_2025-12-09.json

  index-by-date/
    2025-12-09/
      acme_report_2025-12-09.json → ../../files/acme_report_2025-12-09.json

🔧 Technologies Used

Java 17+

Spring Boot

Jackson (ObjectMapper, XmlMapper)

Symbolic link indexing via Java NIO

Custom filename validation

Checkstyle formatting rules

🔐 Filename Format
<customer>_<type>_<yyyy-MM-dd>.xml


Regex:

^[a-zA-Z0-9]+_[a-zA-Z0-9]+_\\d{4}-\\d{2}-\\d{2}\\.xml$


Segments:

Segment	Description
customer	Client/company
type	Category/type
date	ISO date
