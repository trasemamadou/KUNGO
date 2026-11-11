package com.kungo.common.util;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction du stockage de fichiers (avatars, documents, médias de prestation).
 * Implémentation par défaut : disque local ({@link LocalFileStorageService}).
 * Une implémentation S3/Cloudinary peut être substituée en production sans impacter
 * les modules appelants (user, professional, service).
 */
public interface FileStorageService {

    /**
     * Stocke le fichier sous le sous-dossier donné et retourne l'URL publique relative.
     */
    String store(MultipartFile file, String subFolder);

    void delete(String relativeUrl);
}
