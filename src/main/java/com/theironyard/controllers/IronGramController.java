package com.theironyard.controllers;

import com.theironyard.entities.Photo;
import com.theironyard.entities.User;
import com.theironyard.services.PhotoRepository;
import com.theironyard.services.UserRepository;
import com.theironyard.utilities.PasswordStorage;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
public class IronGramController {

    @Autowired
    UserRepository users;

    @Autowired
    PhotoRepository photos;

    Server dbui = null;

    @PostConstruct
    public void init() throws SQLException {
        dbui = Server.createWebServer().start();
    }

    @PreDestroy
    public void destroy() {
        dbui.stop();
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public User login(String username, String password, HttpSession session, HttpServletResponse response)
            throws Exception {
        User user = users.findFirstByName(username);
        if (user == null) {
            user = new User(username, PasswordStorage.createHash(password));
            users.save(user);
        } else if (!PasswordStorage.verifyPassword(password, user.getPassword())) {
            throw new Exception("Wrong password!");
        }
        session.setAttribute("username", username);
        response.sendRedirect("/");
        return user;
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout(HttpSession session, HttpServletResponse response) throws IOException {
        session.invalidate();
        response.sendRedirect("/");
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public User getUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        return users.findFirstByName(username);
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public Photo upload(MultipartFile photo, HttpSession session, HttpServletResponse response,
                        String receiver, int showTime, boolean makePublic) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in!");
        }
        User senderUser = users.findFirstByName(username);
        User receiverUser = users.findFirstByName(receiver);
        if (receiverUser == null) {
            throw new Exception("Receiver name does not exist!");
        }
        if (!photo.getContentType().startsWith("image")) {
            throw new Exception("Only images are allowed!");
        }
        File photoFile = File.createTempFile("photo", photo.getOriginalFilename(), new File("public"));
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes());

        Photo p = new Photo(photoFile.getName(), senderUser, receiverUser, showTime, makePublic);
        photos.save(p);

        if (p.isMakePublic() == true) {
            System.out.println("The photo has been marked as public!");
        }
        response.sendRedirect("/");
        return p;
    }


    @RequestMapping("/photosDelete")
    public List<Photo> photosDelete(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in");
        }
        User user = users.findFirstByName(username);
        int showTime = photos.findByReceiver(user).get(0).getShowTime() * 1000;
        Thread.sleep(showTime);
        photos.deleteByReceiver(user);
        return photos.findByReceiver(user);
    }

    @RequestMapping("/public-photos")
    public List<Photo> publicPhotos(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        User user = users.findFirstByName(username);

        return photos.findByReceiverAndMakePublicIsTrue(user);
    }
}
