package service;

import model.File;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.FileRepository;

@Service("fileService")
public class FileService {
    @Autowired
    private FileRepository repository;

    public void test(User user) {
        repository.save(new File(user, "fname", "1234"));
    }

    public File get() {
        return repository.findByPath("1234");
    }
}
