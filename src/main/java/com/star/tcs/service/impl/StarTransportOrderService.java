package com.star.tcs.service.impl;
/**
 * Title: StarTransportOrderService
 * 功能：相关服务订单实现
 * author: star
 * Creation time: 2020-4-4 21:24
 * Modification time：
 * version： V1.0
 */
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.star.tcs.service.IStarTransportOrderService;
import com.star.tcs.util.KopenTCSUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StarTransportOrderService implements IStarTransportOrderService {

    private static Logger logger = LogManager.getLogger(StarVehicleService.class);
    private static KernelServicePortal kernelServicePortal = null;
    private static TransportOrderService orderService = null;
    private static TransportOrder transportOrder = null;
    private static DispatcherService dispatcherService = null;
    /**
     * 静态，获取
     */
    static {
        kernelServicePortal = KopenTCSUtil.getKernelServicePortal();
        orderService = kernelServicePortal.getTransportOrderService();
        dispatcherService = kernelServicePortal.getDispatcherService();
    }

    //Load cargo
    @Override
    public String requestToLoadGoods(String locationName) {

        List<DestinationCreationTO> destinations = new LinkedList<>();
        destinations.add(new DestinationCreationTO(locationName, "Load cargo"));
        TransportOrderCreationTO orderTO
                = new TransportOrderCreationTO("MyTransportOrder", destinations);
        // Optionally, express that the actual/full name of the order should be
        // generated by the kernel.
        orderTO = orderTO.withIncompleteName(true);
        // Optionally, assign a specific vehicle to the transport order:
        //orderTO = orderTO.withIntendedVehicleName("Some vehicle name");
        // Optionally, set a deadline for the transport order:
        orderTO = orderTO.withDeadline(Instant.now().plus(1, ChronoUnit.HOURS));

        // Create a new transport order for the given description:
        TransportOrder newOrder = orderService.createTransportOrder(orderTO);

        // Trigger the dispatch process for the created transport order.
        dispatcherService.dispatch();
        return newOrder.getName();
    }

    //Unload cargo
    @Override
    public String requestToUnload(String locationName) {
        List<DestinationCreationTO> destinations = new LinkedList<>();
        destinations.add(new DestinationCreationTO(locationName, "Unload cargo"));
        TransportOrderCreationTO orderTO
                = new TransportOrderCreationTO("MyTransportOrder", destinations);
        // Optionally, express that the actual/full name of the order should be
        // generated by the kernel.
        orderTO = orderTO.withIncompleteName(true);
        // Optionally, assign a specific vehicle to the transport order:
        //orderTO = orderTO.withIntendedVehicleName("Some vehicle name");
        // Optionally, set a deadline for the transport order:
        orderTO = orderTO.withDeadline(Instant.now().plus(1, ChronoUnit.HOURS));

        // Create a new transport order for the given description:
        TransportOrder newOrder = orderService.createTransportOrder(orderTO);

        // Trigger the dispatch process for the created transport order.
        dispatcherService.dispatch();
        return newOrder.getName();
    }

    @Override
    public String getCreationTimeByName(String orderName) {
        transportOrder = getTransportOrder(orderName);
        long creationTime = transportOrder.getCreationTime();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
        String ct = sdf.format(new Date(creationTime));  // 时间戳转换成时间     (这里也是毫秒时间戳)
        return ct;
    }

    @Override
    public String getFinishedTimeByName(String orderName) {
        transportOrder = getTransportOrder(orderName);
        long finishedTime = transportOrder.getFinishedTime();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
        String ct = sdf.format(new Date(finishedTime));  // 时间戳转换成时间     (这里也是毫秒时间戳)
        return ct;
    }

    @Override
    public String getAllOrder() {
        return null;
    }

    @Override
    public String getProcessingVehicle(String orderName) {
        transportOrder = getTransportOrder(orderName);
        TCSObjectReference<Vehicle> vehicles = transportOrder.getProcessingVehicle();
        return vehicles.getName();
    }

    @Override
    public String getPathByName(String orderName) {
        transportOrder = getTransportOrder(orderName);
        DriveOrder driveOrder = transportOrder.getCurrentDriveOrder();
        Route route = driveOrder.getRoute();
        List<Route.Step> routes = route.getSteps();
        List<String> paths = new ArrayList<>();
        for (Route.Step step : routes) {
            paths.add(step.getDestinationPoint().getName());
        }
        ObjectMapper mapper = new ObjectMapper();
        String pathString = "";
        try {
            pathString = mapper.writeValueAsString(paths);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return pathString;
    }

    @Override
    public String getStateByName(String orderName) {
        transportOrder = getTransportOrder(orderName);
        return transportOrder.getState().name();
    }

    public static TransportOrder getTransportOrder(String name) {
        return orderService.fetchObject(TransportOrder.class,name);
    }
}