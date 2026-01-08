/**
 * IP Address Geolocation feature module.
 * 
 * Provides IP geolocation services using external providers (IPLocalize.com, IPLocate.io).
 * Supports adapter pattern for multiple provider support with fallback mechanism.
 * 
 * Main components:
 * - Service layer: IPGeolocationService for geolocation lookups
 * - Provider adapters: IPLocalizeAdapter, IPLocateAdapter (future)
 * - DTOs: IPGeolocationData for geolocation information
 * - Entity: IPGeolocationProvider enum for provider identification
 */
package com.hafizbahtiar.spring.features.ipaddress;

