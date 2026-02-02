atualizar a tabela album_images e adicionar os seguintes campos:

- bucket VARCHAR(255)
- hash VARCHAR(255)
- content_type VARCHAR(255) // mimetype
- size INT // tamanho em bytes o arquivo
  remova a coluna image_key

o hash deve ser gerado usando estes metodos:

for (FileUpload file : files) {
String extension = getExtension(file.fileName());
String hash = generateHash(extension);
}

private String getExtension(String fileName) {
if (fileName == null || !fileName.contains(".")) {
return "";
}
return fileName.substring(fileName.lastIndexOf("."));
}

private String generateHash(String extension) {
LocalDate now = LocalDate.now();
String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
String uuid = UUID.randomUUID().toString();
return String.format("%s/%s%s", datePath, uuid, extension);
}
