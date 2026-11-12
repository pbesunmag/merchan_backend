package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.response.CuponDescuentoDTO;
import avengersshop.merchan_backend.dto.response.PromocionResponseDTO;
import avengersshop.merchan_backend.models.CuponDescuento;
import avengersshop.merchan_backend.models.Promocion;
import avengersshop.merchan_backend.repositories.CuponDescuentoRepository;
import avengersshop.merchan_backend.repositories.PromocionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionCuponServiceTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private CuponDescuentoRepository cuponRepository;

    private PromocionService promocionService;
    private CuponDescuentoService cuponService;

    @BeforeEach
    void setUp() {
        promocionService = new PromocionService(promocionRepository);
        cuponService = new CuponDescuentoService(cuponRepository);
    }

    @Test
    void testPriorizaPromocionRealSobreOperacionRelampago() {
        LocalDateTime ahora = LocalDateTime.now();

        Promocion prueba = Promocion.builder()
                .id(1L)
                .codigo("PROMO_PRUEBA_ACTIVA")
                .nombre("Operación Relámpago S.H.I.E.L.D.")
                .porcentajeDescuento(10)
                .fechaInicio(ahora.minusDays(1))
                .fechaFin(ahora.plusDays(1))
                .activo(true)
                .build();

        Promocion blackFriday = Promocion.builder()
                .id(2L)
                .codigo("BLACK_FRIDAY_WAR")
                .nombre("Infinity Black Friday")
                .porcentajeDescuento(20)
                .fechaInicio(ahora.minusDays(1))
                .fechaFin(ahora.plusDays(2))
                .activo(true)
                .build();

        when(promocionRepository.findPromocionesVigentes(any())).thenReturn(List.of(blackFriday, prueba));

        Optional<PromocionResponseDTO> vigente = promocionService.obtenerPromocionVigente();

        assertTrue(vigente.isPresent());
        assertEquals("BLACK_FRIDAY_WAR", vigente.get().getCodigo());
        assertEquals(20, vigente.get().getPorcentajeDescuento());
    }

    @Test
    void testPriorizaPromocionRealActivadaManualmenteAunqueNoEsteEnRango() {
        LocalDateTime ahora = LocalDateTime.now();

        Promocion prueba = Promocion.builder()
                .id(1L)
                .codigo("PROMO_PRUEBA_ACTIVA")
                .nombre("Operación Relámpago S.H.I.E.L.D.")
                .porcentajeDescuento(10)
                .fechaInicio(ahora.minusDays(1))
                .fechaFin(ahora.plusDays(1))
                .activo(true)
                .build();

        Promocion blackFriday = Promocion.builder()
                .id(2L)
                .codigo("BLACK_FRIDAY_WAR")
                .nombre("Infinity Black Friday")
                .porcentajeDescuento(20)
                .fechaInicio(ahora.plusDays(5))
                .fechaFin(ahora.plusDays(10))
                .activo(true)
                .build();

        // En fecha solo coincide la de prueba
        when(promocionRepository.findPromocionesVigentes(any())).thenReturn(List.of(prueba));
        // Pero manualmente activa está Black Friday
        when(promocionRepository.findByActivoTrueOrderByPorcentajeDescuentoDesc()).thenReturn(List.of(blackFriday, prueba));

        Optional<PromocionResponseDTO> vigente = promocionService.obtenerPromocionVigente();

        assertTrue(vigente.isPresent());
        assertEquals("BLACK_FRIDAY_WAR", vigente.get().getCodigo());
    }

    @Test
    void testDevuelveOperacionRelampagoSiNoHayOtraRealActiva() {
        LocalDateTime ahora = LocalDateTime.now();

        Promocion prueba = Promocion.builder()
                .id(1L)
                .codigo("PROMO_PRUEBA_ACTIVA")
                .nombre("Operación Relámpago S.H.I.E.L.D.")
                .porcentajeDescuento(10)
                .fechaInicio(ahora.minusDays(1))
                .fechaFin(ahora.plusDays(1))
                .activo(true)
                .build();

        when(promocionRepository.findPromocionesVigentes(any())).thenReturn(List.of(prueba));
        when(promocionRepository.findByActivoTrueOrderByPorcentajeDescuentoDesc()).thenReturn(List.of(prueba));

        Optional<PromocionResponseDTO> vigente = promocionService.obtenerPromocionVigente();

        assertTrue(vigente.isPresent());
        assertEquals("PROMO_PRUEBA_ACTIVA", vigente.get().getCodigo());
    }

    @Test
    void testObtenerCuponDestacadoPrimerVigente() {
        LocalDateTime ahora = LocalDateTime.now();

        CuponDescuento inactivo = CuponDescuento.builder()
                .id(1L)
                .codigo("INACTIVO")
                .porcentajeDescuento(5)
                .activo(false)
                .build();

        CuponDescuento caducado = CuponDescuento.builder()
                .id(2L)
                .codigo("CADUCADO")
                .porcentajeDescuento(10)
                .activo(true)
                .fechaInicio(ahora.minusDays(10))
                .fechaFin(ahora.minusDays(1))
                .build();

        CuponDescuento agotado = CuponDescuento.builder()
                .id(3L)
                .codigo("AGOTADO")
                .porcentajeDescuento(15)
                .activo(true)
                .usosMaximos(5)
                .usosActuales(5)
                .build();

        CuponDescuento valido = CuponDescuento.builder()
                .id(4L)
                .codigo("AVENGERS20")
                .porcentajeDescuento(20)
                .activo(true)
                .fechaInicio(ahora.minusDays(1))
                .fechaFin(ahora.plusDays(5))
                .usosMaximos(10)
                .usosActuales(2)
                .build();

        when(cuponRepository.findByActivoTrueOrderByIdAsc()).thenReturn(List.of(caducado, agotado, valido));

        Optional<CuponDescuentoDTO> destacado = cuponService.obtenerCuponDestacado();

        assertTrue(destacado.isPresent());
        assertEquals("AVENGERS20", destacado.get().getCodigo());
        assertEquals(20, destacado.get().getPorcentajeDescuento());
    }

    @Test
    void testObtenerCuponDestacadoVacioSiNoHayDisponibles() {
        when(cuponRepository.findByActivoTrueOrderByIdAsc()).thenReturn(Collections.emptyList());

        Optional<CuponDescuentoDTO> destacado = cuponService.obtenerCuponDestacado();

        assertTrue(destacado.isEmpty());
    }
}
