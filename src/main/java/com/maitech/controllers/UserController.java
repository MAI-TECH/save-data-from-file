package com.maitech.controllers;

import com.maitech.models.UserModel;
import com.maitech.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(value = {"/", "/index"})
    public String home(Model model) {
        model.addAttribute("user", new UserModel());

        List<UserModel> userModels = userService.findUsers();

        model.addAttribute("users", userModels);

        return "views/users";
    }

    @PostMapping(value = "/fileupload")
    public String uploadFile(@ModelAttribute UserModel userModel, RedirectAttributes redirectAttributes) {
        boolean isFlag = userService.saveDataFromUploadFile(userModel.getFile());

        if (isFlag)
            redirectAttributes.addFlashAttribute("successmessage", "File upload successfully!");
        else
            redirectAttributes.addFlashAttribute("errormessage", "File upload not done, Please try again!");

        return "redirect:/";
    }
}
