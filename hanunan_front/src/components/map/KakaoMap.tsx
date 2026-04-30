
'use client';

import { useEffect, useRef } from 'react';
import Script from 'next/script';
import { DisasterMessage, WeatherAlert, FireStation, SafetyFacility } from '@/services/api';

const KAKAO_SDK_URL = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_MAP_KEY}&autoload=false&libraries=services,clusterer`;

interface KakaoMapProps {
  center: { lat: number; lng: number };
  activeCategory: 'DISASTER' | 'SAFETY' | 'REPORT';
  disasterData: DisasterMessage[];
  weatherAlerts: WeatherAlert[];
  fireStations: FireStation[];
  safetyData: SafetyFacility[];
  mapReports: any[];
  onSelectItem: (item: any, type: 'DISASTER' | 'WEATHER' | 'FIRE' | 'SAFETY' | 'REPORT') => void;
}

export default function KakaoMap({ center, activeCategory, disasterData, weatherAlerts, fireStations, safetyData, mapReports, onSelectItem }: KakaoMapProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<any>(null);
  const overlaysRef = useRef<any[]>([]);
  const polygonsRef = useRef<any[]>([]);
  const clustererRef = useRef<any>(null);
  const myLocationOverlayRef = useRef<any>(null);

  const handleMoveToCurrentLocation = () => {
    const { kakao } = window as any;
    if (navigator.geolocation && mapInstance.current) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const locPosition = new kakao.maps.LatLng(pos.coords.latitude, pos.coords.longitude);

          mapInstance.current.panTo(locPosition);

          if (myLocationOverlayRef.current) myLocationOverlayRef.current.setMap(null);
          const content = `<div style="width:18px; height:18px; background:#4C5CA4; border:3px solid white; border-radius:50%; box-shadow:0 0 10px rgba(76,92,164,0.5);"></div>`;
          myLocationOverlayRef.current = new kakao.maps.CustomOverlay({
            position: locPosition,
            content,
            yAnchor: 0.5,
          });
          myLocationOverlayRef.current.setMap(mapInstance.current);
        },
        (err) => {
          alert("위치 정보를 가져올 수 없습니다.");
        }
      );
    }
  };

  const requestLocation = (map: any) => {
    const { kakao } = window as any;
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((pos) => {
        const locPosition = new kakao.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
        map.panTo(locPosition);
        if (myLocationOverlayRef.current) myLocationOverlayRef.current.setMap(null);
        const content = `<div style="width:16px; height:16px; background:#007AFF; border:3px solid white; border-radius:50%; box-shadow:0 0 10px rgba(0,122,255,0.5);"></div>`;
        myLocationOverlayRef.current = new kakao.maps.CustomOverlay({ position: locPosition, content, yAnchor: 0.5 });
        myLocationOverlayRef.current.setMap(map);
      });
    }
  };

  const initMap = () => {
    const { kakao } = window as any;
    kakao.maps.load(() => {
      if (!mapRef.current) return;
      mapInstance.current = new kakao.maps.Map(mapRef.current, {
        center: new kakao.maps.LatLng(center.lat, center.lng),
        level: 5
      });

      clustererRef.current = new kakao.maps.MarkerClusterer({
        map: mapInstance.current,
        averageCenter: true,
        minLevel: 6,
        disableClickZoom: false
      });

      handleMoveToCurrentLocation();

      kakao.maps.event.addListener(mapInstance.current, 'click', () => onSelectItem(null, 'DISASTER'));
      renderItems();
    });
  };
  const renderItems = () => {
    const { kakao } = window as any;
    if (!mapInstance.current) return;

    [...overlaysRef.current, ...polygonsRef.current].forEach(item => item.setMap(null));
    overlaysRef.current = [];
    polygonsRef.current = [];

    if (clustererRef.current) {
      clustererRef.current.clear();
    }

    // 1. 재난 문자 마커 (Z-Index를 높여서 항상 위로)
    if (activeCategory === 'DISASTER') {
      const markers: any[] = [];

      // A. 재난 문자 마커
      disasterData.forEach(data => {
        const pos = new kakao.maps.LatLng(data.latitude, data.longitude);
        const content = document.createElement('div');
        content.innerHTML = `
          <div style="cursor:pointer; width:30px; height:30px; background:white; border-radius:50%; border:3px solid #FF4B4B; display:flex; align-items:center; justify-content:center; font-size:14px; box-shadow:0 2px 8px rgba(0,0,0,0.3);">
            ⚠️
          </div>`;

        content.onclick = (e) => { 
          e.stopPropagation(); 
          onSelectItem(data, 'DISASTER'); 
          mapInstance.current.panTo(pos); 
        };

        const overlay = new kakao.maps.CustomOverlay({
          position: pos,
          content,
          yAnchor: 0.5,
          zIndex: 10
        });
        
        markers.push(overlay); 
      });

      weatherAlerts.forEach(alert => {
        try {
          const colors = { HIGH: '#FF0000', MID: '#FF9800', LOW: '#FFD700' };
          const alertColor = colors[alert.severity] || '#FF9800';

          const path = JSON.parse(alert.boundaryGeojson).coordinates[0].map((c: any) => new kakao.maps.LatLng(c[1], c[0]));
          const poly = new kakao.maps.Polygon({
            path,
            strokeWeight: 3,
            strokeColor: alertColor,
            fillColor: alertColor,
            fillOpacity: 0.15
          });
          poly.setMap(mapInstance.current);
          polygonsRef.current.push(poly);

          const pos = new kakao.maps.LatLng(alert.latitude, alert.longitude);
          const content = document.createElement('div');
          content.innerHTML = `
            <div style="cursor:pointer; display:flex; align-items:center; background:white; padding:3px 8px 3px 3px; border-radius:20px; border:2px solid ${alertColor}; box-shadow:0 2px 6px rgba(0,0,0,0.2); white-space:nowrap;">
              <div style="width:22px; height:22px; background:${alertColor}; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:12px; margin-right:5px;">📢</div>
              <span style="font-size:11px; font-weight:bold; color:#333;">${alert.alertType}</span>
            </div>`;

          content.onclick = (e) => {
            e.stopPropagation();
            onSelectItem(alert, 'WEATHER');
            mapInstance.current.panTo(pos);
          };

          const overlay = new kakao.maps.CustomOverlay({
            position: pos,
            content,
            yAnchor: 0.7,
            zIndex: 5
          });
          
          markers.push(overlay); 
        } catch (e) { }
      });

      if (clustererRef.current) {
        clustererRef.current.addMarkers(markers);
      }
    }

    // 주변 안전 시설
    if (activeCategory === 'SAFETY') {
      const markers: any[] = [];

      safetyData?.forEach((facility) => {
        const pos = new kakao.maps.LatLng(facility.latitude, facility.longitude);
        const icon = facility.type === 'FIRE_HYDRANT' ? '🚨' : facility.type === 'SHELTER' ? '🏠' : '⚡';

        const content = document.createElement('div');
        content.innerHTML = `
          <div style="cursor:pointer; background:white; color:#333; padding:5px 10px; border:2px solid #4C5CA4; border-radius:20px; font-weight:bold; font-size:12px; display:flex; align-items:center; gap:4px; box-shadow:0 2px 6px rgba(0,0,0,0.15);">
            <span style="font-size:14px;">${icon}</span>
            <span>${facility.name}</span>
          </div>`;

        content.onclick = (e) => {
          e.stopPropagation();
          onSelectItem(facility, 'SAFETY');
          mapInstance.current.panTo(pos);
        };

        const overlay = new kakao.maps.CustomOverlay({
          position: pos,
          content,
          yAnchor: 1
        });

        markers.push(overlay);
      });

      if (clustererRef.current) {
        clustererRef.current.addMarkers(markers);
      }
    }

    if (activeCategory === 'REPORT') {
      const markers: any[] = []; 

      mapReports.forEach((report) => {
        const pos = new kakao.maps.LatLng(report.latitude, report.longitude);
        const icon = report.type.split(' ')[0];

        const content = document.createElement('div');
        content.innerHTML = `
        <div style="cursor:pointer; background:white; padding:5px 12px; border-radius:20px; border:2px solid #FF8A00; box-shadow:0 2px 8px rgba(0,0,0,0.2); display:flex; align-items:center; gap:5px;">
          <span style="font-size:14px;">${icon}</span>
          <span style="font-size:12px; font-weight:bold; color:#333;">${report.type.split(' ')[1]} 제보</span>
        </div>`;

        content.onclick = (e) => {
          e.stopPropagation();
          onSelectItem(report, 'REPORT');
          mapInstance.current.panTo(pos);
        };

        const overlay = new kakao.maps.CustomOverlay({
          position: pos,
          content,
          yAnchor: 1.2,
          zIndex: 30
        });

        markers.push(overlay); 
      });

      if (clustererRef.current) {
        clustererRef.current.addMarkers(markers);
      }
    }
  };

  useEffect(() => {
    if (mapInstance.current) renderItems();
  }, [disasterData, weatherAlerts, mapReports, activeCategory]);

  return (
    <div className="relative w-full h-full">
      <Script src={KAKAO_SDK_URL} strategy="afterInteractive" onLoad={initMap} />

      <button
        onClick={(e) => {
          e.stopPropagation();
          handleMoveToCurrentLocation();
        }}
        className="absolute bottom-24 right-6 z-[20] bg-white p-3 rounded-full shadow-xl border border-gray-100 hover:bg-gray-50 active:scale-95 transition-all"
        title="현위치로 이동"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#4C5CA4" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
          <circle cx="12" cy="10" r="3"></circle>
        </svg>
      </button>

      <style>{`
        .kakao_scale, .kakao_copyright, [style*="z-index: 1; margin: 0px 6px;"] {
          display: none !important;
        }
      `}</style>

      <div
        ref={mapRef}
        className="absolute inset-0 w-full h-full"
      />
    </div>
  );
}