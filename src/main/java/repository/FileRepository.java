package repository;

import model.File;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;


public interface FileRepository extends CrudRepository<File, UUID> {
    File findByPath(String path);
}