package de.george.navmap.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.teamunify.i18n.I;
import com.vividsolutions.jts.geom.Geometry;

import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.navmap.util.GeoUtil;
import one.util.streamex.StreamEx;

public abstract class Zone implements Comparable<Zone> {

	private Geometry polygon, linearRing;

	protected String guid;
	// Sticks in world coordinates
	protected List<bCVector> points;
	protected bCMatrix worldMatrix;
	// (stick - offset)²
	protected float radius;
	// Offset zur Position der Zone um einen minimalen Radius zu bekommen
	protected bCVector radiusOffset;
	protected boolean ccw;

	protected Zone(String guid, List<bCVector> stickList, bCMatrix worldMatrix, float radius, bCVector radiusOffset, boolean ccw) {
		this.guid = guid;
		points = stickList;
		this.worldMatrix = worldMatrix;
		this.radius = radius;
		this.radiusOffset = radiusOffset;
		this.ccw = ccw;
	}

	public Geometry getPolygon() {
		if (polygon == null) {
			polygon = GeoUtil.FACTORY.createPolygon(GeoUtil.createLinearRing(GeoUtil.create2DCoordinateArray(points)));
		}
		return polygon;
	}

	public Geometry getLinearRing() {
		if (linearRing == null) {
			linearRing = GeoUtil.createLinearRing(GeoUtil.create2DCoordinateArray(points));
		}
		return linearRing;
	}

	public Geometry getMaxExtent(boolean transform) {
		bCVector base = transform ? radiusOffset.getTransformed(worldMatrix) : radiusOffset;
		bCVector max = StreamEx.of(points).maxBy(s -> s.getInvTranslated(base).length()).get();
		return GeoUtil.FACTORY.createLineString(GeoUtil.create2DCoordinateArray(base, max));
	}

	public Geometry getRadiusShape(boolean transform) {
		GeoUtil.SHAPE_FACTORY.setCentre(GeoUtil.to2DCoordinate(transform ? radiusOffset.getTransformed(worldMatrix) : radiusOffset));
		GeoUtil.SHAPE_FACTORY.setSize(radius * 2);
		return GeoUtil.SHAPE_FACTORY.createCircle();
	}

	private void clearCachedGeometry() {
		polygon = null;
		linearRing = null;
	}

	private static Optional<Float> setFirstIntersection(float fIntersect1_Fraction, float fIntersect2_Fraction,
			bCVector2 TestStretchVector2D) {
		if (TestStretchVector2D.getMagnitude() * (fIntersect2_Fraction - fIntersect1_Fraction) <= 1.0) {
			return Optional.empty();
		}

		return Optional.of(TestStretchVector2D.getMagnitude() * fIntersect1_Fraction);
	}

	public Optional<Float> intersectsStretch(bCVector start, bCVector end, boolean dontRequireStartAndEndInZone) {
		bCVector2 TestStretchStart2D = new bCVector2(start.getX(), start.getZ());
		bCVector2 TestStretchEnd2D = new bCVector2(end.getX(), end.getZ());
		bCVector2 TestStretchVector2D = TestStretchEnd2D.getInvTranslated(TestStretchStart2D);

		// TestStretchOrthogonal is perpendicular to TestStretchVector2D
		bCVector2 TestStretchOrthogonal = new bCVector2(end.getZ() - start.getZ(), start.getX() - end.getX());
		if (TestStretchOrthogonal.getSquareMagnitude() <= 0.0000099999997f) {
			return Optional.empty();
		}
		TestStretchOrthogonal.normalize(); // NormalizeSafe(), but shouldn't make a difference.

		bCVector2 FinalDiffVector = null;

		// On which side of the stretch is the point?
		int iPointCount = getPointCount();
		float[] arrPerPointSide = new float[iPointCount];
		boolean bFoundGreaterZero = false;
		boolean bFoundLessZero = false;
		for (int i = 0; i < iPointCount; i++) {
			bCVector WorldPoint = getPoints().get(i);
			bCVector2 WorldPoint2D = new bCVector2(WorldPoint.getX(), WorldPoint.getZ());
			bCVector2 DiffVector = TestStretchStart2D.getInvTranslated(WorldPoint2D);
			FinalDiffVector = DiffVector;

			if (DiffVector.getX() != 0.0f || DiffVector.getY() != 0.0f) {
				arrPerPointSide[i] = DiffVector.getDotProduct(TestStretchOrthogonal);

				bFoundGreaterZero |= arrPerPointSide[i] > 0.0f;
				bFoundLessZero |= arrPerPointSide[i] < 0.0f;
			} else {
				arrPerPointSide[i] = 0.0f;
			}
		}

		if (!bFoundGreaterZero || !bFoundLessZero) {
			return Optional.empty();
		}

		float fIntersect1_Fraction = 1.0f;
		bCVector2 Intersect1_Vector;
		int iIntersect1_StartPoint = -1; // TODO: What should be the standard value? (access of
											// undefined variable otherwise)
		int iIntersect1_EndPoint = -1; // TODO: What should be the standard value? (access of
										// undefined variable otherwise)

		float fIntersect2_Fraction = -0.0000099999997f;
		bCVector2 Intersect2_Vector;
		int iIntersect2_StartPoint;
		int iIntersect2_EndPoint;

		// This positive / negative thing with perpendicular vector could be to determine on which
		// side of a line the point lies.
		// https://math.stackexchange.com/a/3224901
		// https://math.stackexchange.com/a/274728

		// Maybe compiler unroll?

		// arrPerPointSide[iPointCount - 1] and arrPerPointSide[0] are on different sides of the
		// stretch.
		if (arrPerPointSide[iPointCount - 1] * arrPerPointSide[0] <= 0 && 0.0f != arrPerPointSide[iPointCount - 1]
				&& 0.0f != arrPerPointSide[0]
				// arrPerPointSide[iPointCount - 2] and arrPerPointSide[0] are on different sides of
				// the stretch, arrPerPointSide[iPointCount - 1] is on the stretch. (but the
				// PointCount check is strange, at most two points)
				// 1: arrPerPointSide[0] == 0.0f && arrPerPointSide[-1] * arrPerPointSide[0] <= 0.0f
				// ILLEGAL ACCESS!
				// 2: arrPerPointSide[1] == 0.0f && arrPerPointSide[0] * arrPerPointSide[0] <= 0.0f
				// ->
				// arrPerPointSide[0] and arrPerPointSide[1] are on the stretch
				|| arrPerPointSide[iPointCount - 1] == 0.0f && iPointCount - 2 <= 0
						&& arrPerPointSide[iPointCount - 2] * arrPerPointSide[0] <= 0.0f
				// arrPerPointSide[iPointCount - 1] and arrPerPointSide[1] are on different sides of
				// the stretch, arrPerPointSide[0] is on the stretch. (but the PointCount check is
				// strange, at least two points)
				// 2: arrPerPointSide[0] == 0.0f && arrPerPointSide[1] * arrPerPointSide[1] <= 0.0f
				// ->
				// arrPerPointSide[0] and arrPerPointSide[1] are on the stretch
				// 3: arrPerPointSide[0] == 0.0f && arrPerPointSide[2] * arrPerPointSide[1] <= 0.0f
				// ->
				// arrPerPointSide[1] and arrPerPointSide[2] are on same side of the stretch
				|| arrPerPointSide[0] == 0.0f && iPointCount - 1 >= 1 && 0.0f >= arrPerPointSide[iPointCount - 1] * arrPerPointSide[1]) {
			bCVector PrevPoint = getPoints().get(iPointCount - 1);
			bCVector CurPoint = getPoints().get(0);

			bCVector2 PrevPoint2D = new bCVector2(PrevPoint.getX(), PrevPoint.getZ());
			bCVector2 CurPoint2D = new bCVector2(CurPoint.getX(), CurPoint.getZ());

			bCVector2 PointPrev_To_Cur = CurPoint2D.getInvTranslated(PrevPoint2D);

			float fIntersectFrac;
			if (PointPrev_To_Cur.getX() == 0.0f) {
				if (PrevPoint2D.getX() == TestStretchStart2D.getX()) {
					fIntersectFrac = 0.0f;
				} else {
					fIntersectFrac = (PrevPoint2D.getX() - TestStretchStart2D.getX()) / TestStretchVector2D.getX();
				}
			} else if (PointPrev_To_Cur.getY() == 0.0f) {
				if (PrevPoint2D.getY() == TestStretchStart2D.getY()) {
					fIntersectFrac = 0.0f;
				} else {
					fIntersectFrac = (PrevPoint2D.getY() - TestStretchStart2D.getY()) / TestStretchVector2D.getY();
				}
			} else {
				if (PrevPoint2D.getX() == TestStretchStart2D.getX() && PrevPoint2D.getY() == TestStretchStart2D.getY()) {
					fIntersectFrac = 0.0f;
				} else {
					fIntersectFrac = ((TestStretchStart2D.getX() - PrevPoint2D.getX()) / PointPrev_To_Cur.getX()
							- (TestStretchStart2D.getY() - PrevPoint2D.getY()) / PointPrev_To_Cur.getY())
							/ (TestStretchVector2D.getY() / PointPrev_To_Cur.getY()
									- TestStretchVector2D.getX() / PointPrev_To_Cur.getX());
				}
			}

			if (fIntersect1_Fraction > fIntersectFrac && fIntersectFrac >= 0.0f) {
				fIntersect1_Fraction = fIntersectFrac;
				Intersect1_Vector = TestStretchStart2D.getTranslated(PointPrev_To_Cur.getScaled(fIntersectFrac));
				iIntersect1_StartPoint = iPointCount - 1;
				iIntersect1_EndPoint = 0;
			}

			if (fIntersectFrac > fIntersect2_Fraction && fIntersectFrac <= 1.0f) {
				fIntersect2_Fraction = fIntersectFrac;
				Intersect2_Vector = TestStretchStart2D.getTranslated(PointPrev_To_Cur.getScaled(fIntersectFrac));
				iIntersect2_StartPoint = iPointCount - 1;
				iIntersect2_EndPoint = 0;
			}
		}

		float fFinalIntersect1_Fraction = 1.0f;
		bCVector2 FinalIntersect1_Vector;
		int iFinalIntersect1_StartPoint = -1;
		int iFinalIntersect1_EndPoint = -1;

		if (iPointCount > 1) {
			for (int iPoint = 1; iPoint < iPointCount; iPoint++) {
				float fPrevSide = arrPerPointSide[iPoint - 1];
				float fCurSide = arrPerPointSide[iPoint];

				// On the same side...
				if (fPrevSide * fCurSide > 0.0f) {
					continue;
				}

				boolean bDifferentSides = false;
				if (fPrevSide != 0.0f && fCurSide != 0.0f) {
					bDifferentSides = true;
				}

				if (!bDifferentSides && fPrevSide == 0.0f && iPoint - 2 >= 0) {
					// Previous of previous and current on different side?
					if (arrPerPointSide[iPoint - 2] * fCurSide <= 0.0f) {
						bDifferentSides = true;
					}
				}

				if (!bDifferentSides && fPrevSide == 0.0f && iPoint - 2 < 0) {
					// Previous of previous and current on different side?
					if (arrPerPointSide[iPointCount - 1] * fCurSide <= 0.0f) {
						bDifferentSides = true;
					}
				}

				if (!bDifferentSides && fCurSide == 0.0f && iPoint + 1 <= iPointCount - 1) {
					// Previous and next on different side?
					if (arrPerPointSide[iPoint + 1] * fPrevSide <= 0.0f) {
						bDifferentSides = true;
					}
				}

				if (!bDifferentSides && fCurSide == 0.0f && iPoint + 1 > iPointCount - 1) {
					// Previous and next on different side?
					if (arrPerPointSide[0] * fPrevSide <= 0.0f) {
						bDifferentSides = true;
					}
				}

				// On the same side...
				if (!bDifferentSides) {
					continue;
				}

				bCVector PrevPoint = getPoints().get(iPoint - 1);
				bCVector CurPoint = getPoints().get(iPoint);

				bCVector2 PrevPoint2D = new bCVector2(PrevPoint.getX(), PrevPoint.getZ());
				bCVector2 CurPoint2D = new bCVector2(CurPoint.getX(), CurPoint.getZ());

				bCVector2 PointPrev_To_Cur = CurPoint2D.getInvTranslated(PrevPoint2D);
				FinalDiffVector = PointPrev_To_Cur;

				float fIntersectFrac;
				if (arrPerPointSide[iPoint - 1] != 0.0f && arrPerPointSide[iPoint] != 0.0f) {
					if (PointPrev_To_Cur.getX() == 0.0f) {
						if (PrevPoint2D.getX() == TestStretchStart2D.getX()) {
							fIntersectFrac = 0.0f;
						} else {
							fIntersectFrac = (PrevPoint2D.getX() - TestStretchStart2D.getX()) / TestStretchVector2D.getX();
						}
					} else if (PointPrev_To_Cur.getY() == 0.0f) {
						if (PrevPoint2D.getY() == TestStretchStart2D.getY()) {
							fIntersectFrac = 0.0f;
						} else {
							fIntersectFrac = (PrevPoint2D.getY() - TestStretchStart2D.getY()) / TestStretchVector2D.getY();
						}
					} else {
						if (PrevPoint2D.getX() == TestStretchStart2D.getX() && PrevPoint2D.getY() == TestStretchStart2D.getY()) {
							fIntersectFrac = 0.0f;
						} else {
							fIntersectFrac = ((TestStretchStart2D.getX() - PrevPoint2D.getX()) / PointPrev_To_Cur.getX()
									- (TestStretchStart2D.getY() - PrevPoint2D.getY()) / PointPrev_To_Cur.getY())
									/ (TestStretchVector2D.getY() / PointPrev_To_Cur.getY()
											- TestStretchVector2D.getX() / PointPrev_To_Cur.getX());
						}
					}
				} else {
					fIntersectFrac = 0.0f;
				}

				if (fIntersect1_Fraction > fIntersectFrac && fIntersectFrac >= 0.0f) {
					fFinalIntersect1_Fraction = fIntersect1_Fraction;
					iFinalIntersect1_StartPoint = iIntersect1_StartPoint;
					iFinalIntersect1_EndPoint = iIntersect1_EndPoint;

					fIntersect1_Fraction = fIntersectFrac;
					Intersect1_Vector = TestStretchStart2D.getTranslated(PointPrev_To_Cur.getScaled(fIntersectFrac));
					iIntersect1_StartPoint = iPoint - 1;
					iIntersect1_EndPoint = iPoint;
				} else if (fFinalIntersect1_Fraction > fIntersectFrac && fIntersectFrac > 0.0f) {
					// Strange local variables, are later assigned to the class variables...
					fFinalIntersect1_Fraction = fIntersectFrac;
					iFinalIntersect1_StartPoint = iPoint - 1;
					iFinalIntersect1_EndPoint = iPoint;
				}

				if (fIntersectFrac > fIntersect2_Fraction && fIntersectFrac <= 1.0f) {
					fIntersect2_Fraction = fIntersectFrac;
					Intersect2_Vector = TestStretchStart2D.getTranslated(PointPrev_To_Cur.getScaled(fIntersectFrac));
					iIntersect2_StartPoint = iPoint - 1;
					iIntersect2_EndPoint = iPoint;
				}
			}
		}

		if (dontRequireStartAndEndInZone) {
			return Optional.of(TestStretchVector2D.getMagnitude() * fIntersect1_Fraction);
		}

		// Everything from here is only valid for NavZones (has to be mirrored for NegZones?)
		if (fIntersect1_Fraction >= 1.0f || fIntersect1_Fraction == fIntersect2_Fraction) {
			return Optional.empty();
		}

		if (isCcw() && arrPerPointSide[iIntersect1_StartPoint] <= 0.0f && arrPerPointSide[iIntersect1_EndPoint] >= 0.0f
				|| !isCcw() && arrPerPointSide[iIntersect1_StartPoint] >= 0.0f && arrPerPointSide[iIntersect1_EndPoint] <= 0.0f) {
			float fCurrentSide = 0.0f;
			if (fCurrentSide != arrPerPointSide[iIntersect1_StartPoint] && fCurrentSide != arrPerPointSide[iIntersect1_EndPoint]) {
				return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
			}

			if (fCurrentSide == arrPerPointSide[iIntersect1_StartPoint]) {
				if (iIntersect1_StartPoint > 0) {
					if (isCcw()) {
						if (arrPerPointSide[iIntersect1_StartPoint - 1] <= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					} else {
						if (arrPerPointSide[iIntersect1_StartPoint - 1] >= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					}
				} else {
					if (isCcw()) {
						if (arrPerPointSide[iPointCount - 1] <= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					} else {
						if (arrPerPointSide[iPointCount - 1] >= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					}
				}
			} else if (fCurrentSide == arrPerPointSide[iIntersect1_EndPoint]) {
				if (iPointCount - 1 <= iIntersect1_EndPoint) {
					if (isCcw()) {
						if (arrPerPointSide[0] >= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					} else {
						if (arrPerPointSide[0] <= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					}
				} else {
					if (isCcw()) {
						if (arrPerPointSide[iIntersect1_EndPoint + 1] >= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					} else {
						if (arrPerPointSide[iIntersect1_EndPoint + 1] <= fCurrentSide) {
							return setFirstIntersection(fIntersect1_Fraction, fIntersect2_Fraction, TestStretchVector2D);
						}
					}
				}
			}
		}

		fIntersect1_Fraction = fFinalIntersect1_Fraction;
		iIntersect1_StartPoint = iFinalIntersect1_StartPoint; // Unitialized use, the result is
																// wrong! fMyIntersectionPercentage
																// -107374176. float

		iIntersect1_EndPoint = iFinalIntersect1_EndPoint;
		Intersect1_Vector = TestStretchStart2D.getTranslated(FinalDiffVector.getScaled(fFinalIntersect1_Fraction));
		if (fIntersect1_Fraction == fIntersect2_Fraction) {
			return Optional.empty();
		}

		return Optional.of(TestStretchVector2D.getMagnitude() * fIntersect1_Fraction);
	}

	// @foff
	// isLeft(): tests if a point is Left|On|Right of an infinite line.
	//    Input:  three points L1, L2, and P
	//    Return: >0 for P left of the line through L1 and L2
	//            =0 for P on the line
	//            <0 for P right of the line
	//    See: Algorithm 1 "Area of Triangles and Polygons"
	// @fon
	private float isLeft(bCVector L1, bCVector L2, bCVector P) {
		// a_Point left of line
		return (L2.getX() - L1.getX()) * (P.getZ() - L1.getZ()) - (P.getX() - L1.getX()) * (L2.getZ() - L1.getZ());
	}

	// https://www.geomalgorithms.com/a03-_inclusion.html
	public boolean test3DPointInZone(bCVector position) {
		// If Position in 2D is equal to Points[-1] or Points[0] -> return
		// a_bRelatedToIntersectWithDCC
		// or if Position is really close to the first stretch
		// But why would we want to handle the first stretch specially, in the case of DCCs?!

		// a_bRelatedToIntersectWithDCC is GETrue normally, but for a few calls related to DCCs it
		// is GEFalse...

		int wn = 0; // the winding number counter

		// TODO: Original code transforms Position into NavZone local coordinate system
		bCVector LocalPosition = position;

		// a_bIncludeBorder seems to be something entirely different
		// TODO: CCW? I guess it doesn't matter...

		// loop through all edges of the polygon
		for (int s = points.size() - 1, e = 0; e < points.size(); s = e++) {
			// edge from V[s] to V[e]
			if (points.get(s).getZ() <= LocalPosition.getZ()) { // start y <= P.y
				if (points.get(e).getZ() > LocalPosition.getZ()) // an upward crossing
				{
					float l = isLeft(points.get(s), points.get(e), LocalPosition);
					if (l > 0.0f) {
						++wn; // have a valid up intersect
					} else if (l == 0.0f) {
						return true;
					}
				}
			} else { // start y > P.y (no test needed)
				if (points.get(e).getZ() <= LocalPosition.getZ()) // a downward crossing
				{
					float l = isLeft(points.get(s), points.get(e), LocalPosition);
					if (l < 0.0f) {
						--wn; // have a valid down intersect
					} else if (l == 0.0f) {
						return true;
					}
				}
			}
		}

		// the point is outside only iff the winding number is 0
		return wn != 0;
	}

	@Override
	public int compareTo(Zone o) {
		if (radius < o.radius) {
			return -1;
		}
		if (radius > o.radius) {
			return 1;
		}
		return 0;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public int getPointCount() {
		return points.size();
	}

	public List<bCVector> getPoints() {
		return Collections.unmodifiableList(points);
	}

	public abstract List<bCVector> getWorldPoints();

	public void setPoints(List<bCVector> points) {
		this.points = points;
		clearCachedGeometry();
	}

	public bCMatrix getWorldMatrix() {
		return worldMatrix;
	}

	public void setWorldMatrix(bCMatrix worldMatrix) {
		this.worldMatrix = worldMatrix;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public bCVector getRadiusOffset() {
		return radiusOffset;
	}

	public abstract bCVector getWorldRadiusOffset();

	public void setRadiusOffset(bCVector radiusOffset) {
		this.radiusOffset = radiusOffset;
	}

	public boolean isCcw() {
		return ccw;
	}

	public void setCcw(boolean ccw) {
		this.ccw = ccw;
	}

	public boolean isLinearRingValid() {
		return points.size() >= 3 && getLinearRing().isValid();
	}

	public boolean isValid() {
		return points.size() >= 3;
	}

	public static Optional<Boolean> arePointsCcw(List<bCVector> points) {
		if (points.size() < 3) {
			return Optional.empty();
		}

		float area = 0.0f;
		for (int s = points.size() - 1, e = 0; e < points.size(); s = e++) {
			area += (points.get(e).getX() - points.get(s).getX()) * (points.get(e).getZ() + points.get(s).getZ());
		}
		return Optional.of(area < 0.0f);
	}

	public Optional<Boolean> arePointsCcw() {
		return arePointsCcw(points);
	}

	public List<String> generateErrors() {
		List<String> errors = new LinkedList<>();

		if (getPointCount() < 3) {
			errors.add(I.trf("Zone needs at least 3 sticks, but has only {0, number}.", getPointCount()));
		} else if (!isLinearRingValid()) {
			errors.add(I.tr("Zone not valid in 2D, but may be valid in 3D."));
		} else if (isCcw() != arePointsCcw().get()) {
			errors.add(I.tr("Orientation of the sticks does not correspond to the CCW property"));
		}

		return errors;
	}
}
