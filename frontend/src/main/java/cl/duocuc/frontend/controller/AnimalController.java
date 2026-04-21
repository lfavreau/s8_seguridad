package cl.duocuc.frontend.controller;

import cl.duocuc.frontend.model.Animal;
import cl.duocuc.frontend.service.AnimalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.client.RestClientException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/animales")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        if (!hasToken(session)) {
            return "redirect:/login?expired";
        }
        try {
            model.addAttribute("animales", animalService.getPrivateAnimals(session));
        } catch (RestClientException ex) {
            model.addAttribute("animales", java.util.Collections.emptyList());
            model.addAttribute("backendError", true);
        }
        return "animales";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model, HttpSession session) {
        if (!hasToken(session)) {
            return "redirect:/login?expired";
        }
        model.addAttribute("animal", new Animal());
        model.addAttribute("accion", "Crear");
        return "animal-form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Animal animal, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!hasToken(session)) {
            return "redirect:/login?expired";
        }
        try {
            animalService.createAnimal(animal, session);
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("backendError", true);
        }
        return "redirect:/animales";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, HttpSession session) {
        if (!hasToken(session)) {
            return "redirect:/login?expired";
        }
        model.addAttribute("animal", animalService.getAnimalById(id, session));
        model.addAttribute("accion", "Actualizar");
        return "animal-edit";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Animal animal, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!hasToken(session)) {
            return "redirect:/login?expired";
        }
        try {
            animalService.updateAnimal(id, animal, session);
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("backendError", true);
        }
        return "redirect:/animales";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!hasToken(session)) {
            return "redirect:/login?expired";
        }
        try {
            animalService.deleteAnimal(id, session);
        } catch (RestClientException ex) {
            redirectAttributes.addFlashAttribute("backendError", true);
        }
        return "redirect:/animales";
    }

    private boolean hasToken(HttpSession session) {
        return session.getAttribute("JWT_TOKEN") != null;
    }
}
