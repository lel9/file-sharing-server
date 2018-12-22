package service;

import model.File;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.FileRepository;

import java.util.Optional;
import java.util.UUID;

@Service("fileService")
public class FileService {
    @Autowired
    private FileRepository repository;

    public File save(User user, String name, String initDir) {
        File file = new File(user, name, initDir);
        repository.save(file);
        return file;
    }

    public Optional<File> findById(String id) {
        return repository.findById(UUID.fromString(id));
    }
}
