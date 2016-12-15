--SELECT * FROM fn_consulta_resumen_ordenes('2014-10-9') where o_idsucursal=2

DROP FUNCTION fn_consulta_resumen_ordenes(IN in_fecha date);
CREATE OR REPLACE FUNCTION fn_consulta_resumen_ordenes(IN in_fecha date)
  RETURNS TABLE(o_nombre character varying, o_idsucursal integer, o_cantida_recibida integer, o_tiempo_total numeric, o_tiempo_dias numeric, o_total_gaveta integer, o_total_laboratorio integer,
  o_tot_examen integer, o_tot_ordenes_examen integer, o_total_cot_examenes integer, o_tot_examen_sin_receta integer, o_tot_examen_con_receta integer) AS
$BODY$
BEGIN
RETURN QUERY SELECT nombre::character varying as o_nombre, idsucursal as o_idsucursal, SUM(recibidas)::integer as o_cantida_recibida, SUM(tiempototal) as o_tiempo_total, SUM(tiempodias) as o_tiempo_dias,
 SUM(gaveta)::integer as o_total_gaveta, SUM(enviadas)::integer as o_total_laboratorio, 
 SUM(examenes)::integer as o_tot_examen, SUM(ordenes_examen)::integer as o_tot_ordenes_examen, 
 SUM(cotizaciones_examen)::integer as o_total_cot_examenes, SUM(examenes_sinreceta)::integer as o_tot_examen_sin_receta  , SUM(examenes_conreceta)::integer as o_tot_examen_con_receta  
FROM (
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=o.idsucursal) as nombre,idsucursal,COUNT (idseqsolorden)::integer as  recibidas,
		round(SUM(EXTRACT(epoch FROM(( ((fecharecibido::character varying||' '||horarecibido::character varying)::timestamp)- 
		((fecha::character varying||' '||horacrea::character varying)::timestamp) )  /3600))::numeric)/count(idseqsolorden), 2) as tiempototal, 
		round(SUM(EXTRACT(epoch FROM(( ((fecharecibido::character varying||' '||horarecibido::character varying)::timestamp)-
		((fecha::character varying||' '||horacrea::character varying)::timestamp) )  /86400))::numeric)/count(idseqsolorden), 2) as tiempodias,
		0 as gaveta,0 as enviadas, 0 as examenes, 0 as ordenes_examen, 0 as cotizaciones_examen, 0 as examenes_sinreceta, 0 as examenes_conreceta
	FROM otl_solorordenproduccion o
	WHERE fecha is not null
	AND horacrea is not null
	AND fecharecibido = in_fecha
	GROUP by idsucursal
UNION
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=o.idsucursal) as nombre,idsucursal,0,0,0,COUNT(idseqsolorden),0,0,0,0,0,0
	FROM otl_solorordenproduccion o
	WHERE fecha is not null
	AND fecharecibido is not null
	AND ESTADO='T'
	GROUP by idsucursal
UNION
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=o.idsucursal) as nombre,idsucursal,0,0,0,0,COUNT(idseqsolorden),0,0,0,0,0
	FROM otl_solorordenproduccion o
	WHERE fecha > '2014/01/01'
	AND horacrea is not null
	AND fecharecibido is null
	AND entregado is null
	AND  ESTADO='S'
	GROUP by idsucursal
UNION
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=e.idsucursal) as nombre,idsucursal,0,0,0,0,0,COUNT(idseqexamen),0,0,0,0 
	FROM otl_examen e
	WHERE e.fecha = in_fecha
	--WHERE fecha = '2014-10-10'
	GROUP by idsucursal
UNION
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=c.idsucursal) as nombre,c.idsucursal,0,0,0,0,0,0,COUNT(c.idseqsolorden),0,0,0
	FROM otl_solorordenproduccion c inner join otl_examen e on (e.idseqreceta=c.idseqreceta and e.idsucursal=c.idsucursal and e.fecha=c.fecha) 
	WHERE c.fecha = in_fecha	
	--WHERE c.fecha =  '2014-10-10'
	GROUP by c.idsucursal
UNION
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=c.idsucursal) as nombre,c.idsucursal,0,0,0,0,0,0,0,COUNT(c.idseqcotizacion),0,0
	FROM otl_cotizacion c inner join otl_examen e on (e.idseqreceta=c.idseqreceta and e.idsucursal=c.idsucursal and e.fecha=c.fecha) 
	WHERE c.fecha = in_fecha	
	--WHERE c.fecha =  '2014-10-10'
	GROUP by c.idsucursal
UNION
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=e.idsucursal) as nombre,e.idsucursal,0,0,0,0,0,0,0,0,COUNT(e.idseqexamen),0
	FROM otl_examen e
	WHERE e.fecha = in_fecha	
	--WHERE e.fecha =  '2014-10-10'
	AND (e.idseqreceta is null or e.idseqreceta = 0 )
	GROUP by e.idsucursal
UNION
	SELECT (SELECT titulo FROM otl_sucursal s WHERE s.idsucursal=e.idsucursal) as nombre,e.idsucursal,0,0,0,0,0,0,0,0,0,COUNT(e.idseqexamen)
	FROM otl_examen e
	WHERE e.fecha = in_fecha	
	--WHERE e.fecha =  '2014-10-10'
	AND (e.idseqreceta > 0 )
	GROUP by e.idsucursal
) as s
GROUP by nombre,idsucursal
order by o_idsucursal;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;