package pe.edu.upeu.orden.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.orden.dto.OrdenRequest;
import pe.edu.upeu.orden.dto.OrdenResponse;
import pe.edu.upeu.orden.service.OrdenService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    @GetMapping
    public List<OrdenResponse> listar() {
        return ordenService.listar();
    }

    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable Long id) {
        return ordenService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse crear(@Valid @RequestBody OrdenRequest request) {
        return ordenService.crear(request);
    }

    @PutMapping("/{id}")
    public OrdenResponse actualizar(@PathVariable Long id, @Valid @RequestBody OrdenRequest request) {
        return ordenService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        ordenService.eliminar(id);
    }
}
