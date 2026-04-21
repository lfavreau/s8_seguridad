package cl.duocuc.frontend.controller;

import cl.duocuc.frontend.service.AnimalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.client.RestClientException;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class HomeController {

    private final AnimalService animalService;

    public HomeController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        if (session.getAttribute("JWT_TOKEN") == null) {
            return "redirect:/login?expired";
        }
        try {
            model.addAttribute("animales", animalService.getPrivateAnimals(session));
        } catch (RestClientException ex) {
            model.addAttribute("animales", Collections.emptyList());
            model.addAttribute("backendError", true);
        }
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
