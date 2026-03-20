package request_management.application;

import request_management.domain.Shipment;

public class ValidateShipmentRequestImpl implements ValidateShipmentRequest {

    private static final int MAX_DELIVERY_TIME_HOURS = 1;

    @Override
    public boolean validate(Shipment shipment) {
        return shipment.getDeliveryTimeLimit() <= MAX_DELIVERY_TIME_HOURS;
    }
}