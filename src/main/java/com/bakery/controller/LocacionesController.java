package com.bakery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/locaciones")
public class LocacionesController {

    @GetMapping("/north-bergen")
    public String northBergen(Model model) {
        model.addAttribute("pagina", "locaciones");
        model.addAttribute("canonicalUrl", "https://elfenixbakery.com/locaciones/north-bergen");
        model.addAttribute("locacion", "North Bergen");
        model.addAttribute("direccion", "8133 Bergenline Ave, North Bergen, NJ 07047");
        model.addAttribute("mapsQuery", "El Fenix Bakery, 8133 Bergenline Ave, North Bergen, NJ 07047");
        model.addAttribute("mapsCoords", "40.803757534292856,-74.00530350942387");
        model.addAttribute("telefono", "+1 (201) 994-4060");
        model.addAttribute("telefonoRaw", "+12019944060");
        model.addAttribute("horario1", "Lunes – Jueves: 6:00am – 8:30pm");
        model.addAttribute("horario1En", "Monday – Thursday: 6:00am – 8:30pm");
        model.addAttribute("horario2", "Viernes – Sábado: 6:00am – 9:00pm");
        model.addAttribute("horario2En", "Friday – Saturday: 6:00am – 9:00pm");
        model.addAttribute("heroImg", "/img/loc-north-bergen.jpg");
        model.addAttribute("grubhub", "https://www.grubhub.com/restaurant/fenix-bakery-iii-8133-bergenline-ave-north-bergen/7657888");
        model.addAttribute("ubereats", "https://www.ubereats.com/store/el-fenix-bakery-3/kv2TTiCxVUGKd0p4qJObow?diningMode=DELIVERY");
        return "public/locacion";
    }

    @GetMapping("/west-new-york")
    public String westNewYork(Model model) {
        model.addAttribute("pagina", "locaciones");
        model.addAttribute("canonicalUrl", "https://elfenixbakery.com/locaciones/west-new-york");
        model.addAttribute("locacion", "West New York");
        model.addAttribute("direccion", "6132 Bergenline Ave, West New York, NJ 07093");
        model.addAttribute("mapsQuery", "El Fenix Bakery, 6132 Bergenline Ave, West New York, NJ 07093");
        model.addAttribute("mapsCoords", "40.791367626875264,-74.01460996925532");
        model.addAttribute("telefono", "+1 (201) 854-2262");
        model.addAttribute("telefonoRaw", "+12018542262");
        model.addAttribute("horario1", "Lunes – Sábado: 5:00am – 8:00pm");
        model.addAttribute("horario1En", "Monday – Saturday: 5:00am – 8:00pm");
        model.addAttribute("horario2", "Domingo: 6:00am – 7:00pm");
        model.addAttribute("horario2En", "Sunday: 6:00am – 7:00pm");
        model.addAttribute("heroImg", "/img/loc-west-new-york.png");
        model.addAttribute("grubhub", "https://www.grubhub.com/restaurant/fenix-bakery-ii-6132-bergenline-ave-west-new-york/7657880");
        model.addAttribute("ubereats", "https://www.ubereats.com/store/el-fenix-bakery-ii/dR0MkiCsReO5OoS6slRLsA?diningMode=DELIVERY");
        return "public/locacion";
    }

    @GetMapping("/union-city")
    public String unionCity(Model model) {
        model.addAttribute("pagina", "locaciones");
        model.addAttribute("canonicalUrl", "https://elfenixbakery.com/locaciones/union-city");
        model.addAttribute("locacion", "Union City");
        model.addAttribute("direccion", "4211 Bergenline Ave, Union City, NJ 07087");
        model.addAttribute("mapsQuery", "El Fenix Bakery, 4211 Bergenline Ave, Union City, NJ 07087");
        model.addAttribute("mapsCoords", "40.77817815836558,-74.02502107415496");
        model.addAttribute("telefono", "+1 (201) 864-2699");
        model.addAttribute("telefonoRaw", "+12018642699");
        model.addAttribute("horario1", "Lunes – Sábado: 5:00am – 8:30pm");
        model.addAttribute("horario1En", "Monday – Saturday: 5:00am – 8:30pm");
        model.addAttribute("horario2", "Domingo: 6:00am – 8:30pm");
        model.addAttribute("horario2En", "Sunday: 6:00am – 8:30pm");
        model.addAttribute("heroImg", "/img/loc-union-city.jpg");
        model.addAttribute("grubhub", "https://www.grubhub.com/restaurant/fenix-bakery-i-4211-bergenline-ave-union-city/7656712");
        model.addAttribute("ubereats", "https://www.ubereats.com/store/fenix-bakery-i/XvLlcc0fTbarrwDNgZtuHw?diningMode=DELIVERY");
        return "public/locacion";
    }
}
