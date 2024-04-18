package es.mybopi.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.service.ProductoService;
import es.mybopi.service.UploadFileService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private UploadFileService upload;
    
    @Autowired
    private ProductoService productoService;

    @GetMapping("")
    public String detalles(Model model){
        model.addAttribute("inventario", productoService.findAll());
        return "productos/detalles";
    }

    //Pantalla crear productos
    @GetMapping("/crear")
    public String crearProducto(){
        return "productos/crear";
    }

    //Guardar un nuevo producto
    @PostMapping("/guardar")
    public String guardar(Producto producto, @RequestParam("img1") MultipartFile file) throws IOException{
        LOGGER.info("Guardado {}", producto);
        Usuario u = new Usuario(1, "admin", "admin", "admin", "admin", "admin", null, 1, "admin", null, null);
        producto.setUsuario(u);

        if(producto.getId() == null){ //Creando un producto
            String nombreImagen = upload.saveImage(file);
            producto.setPortada(nombreImagen);
        } 
        productoService.save(producto);
        return "redirect:/productos";
    }

    //Pantalla editar producto
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model){
        Producto producto = new Producto();
        Optional<Producto> optionalProducto = productoService.findById(id);

        producto = optionalProducto.get();

        model.addAttribute("producto", producto);

        return "productos/editar";
    }

    @PostMapping("/actualizar")
    public String actualizar(Producto producto, @RequestParam("img1") MultipartFile portada) throws IOException{

        if(portada.isEmpty()){
            Producto p = new Producto();
            p = productoService.findById(producto.getId()).get();
            producto.setPortada(p.getPortada());
        } else { //Editando cambiando la imagen

            Producto p = new Producto();
            p = productoService.findById(producto.getId()).get();

            if(p.getPortada().equals("default.jpg")){ //Si hay una portada por defecto borrarla
            upload.deleteImage(p.getPortada());
            }
            String nombreImagen = upload.saveImage(portada);
            producto.setPortada(nombreImagen);
        }

        productoService.update(producto);
        return "redirect:/productos";

    }


    //Acción de borrar un producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id) throws IOException{
        Producto p = new Producto();
        p = productoService.findById(id).get();

        if(p.getPortada().equals("default.jpg")){ //Si hay una portada por defecto borrarla
        upload.deleteImage(p.getPortada());

        }
        
        upload.deleteImage(p.getPortada());
        
        productoService.deleteById(id);
        return "redirect:/productos";

    }




}
