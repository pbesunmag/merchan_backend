package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearCategoriaDTO;
import avengersshop.merchan_backend.dto.response.CategoriaDTO;
import avengersshop.merchan_backend.services.AvengersShopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final AvengersShopService avengersShopService;
    public CategoriaController(AvengersShopService avengersShopService) {
        this.avengersShopService = avengersShopService;
    }

    //Obtenemos todas las categorías
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        List<CategoriaDTO> categorias = avengersShopService.listarCategorias();
        return ResponseEntity.status(HttpStatus.OK).body(categorias);
    }

    //Creamos nueva categoría
    // El PAYLOAD es el JSON que viene en el cuerpo de la petición HTTP (con nombre y descripción).
    // La anotación @RequestBody lo recibe y convierte en el objeto CrearCategoriaDTO.
    @PostMapping
    public ResponseEntity<CategoriaDTO> crearCategoria(@Valid @RequestBody CrearCategoriaDTO categoriaDTO) {
        CategoriaDTO nuevaCategoria = avengersShopService.crearCategoria(categoriaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }
}
