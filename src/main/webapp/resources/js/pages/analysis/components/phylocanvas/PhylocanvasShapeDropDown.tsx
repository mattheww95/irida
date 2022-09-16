import { Button, Dropdown, Menu } from "antd";
import React, { useMemo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { updateTreeType } from "../../redux/treeSlice";
import { TreeTypes } from "@phylocanvas/phylocanvas.gl";
import Icon from "@ant-design/icons";
import { CustomIconComponentProps } from "@ant-design/icons/lib/components/Icon";

const RectangleSVG = () => (
  <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
    <path d="M468,144c-11.76,0 -22.211,5.651 -28.782,14.384c-4.531,6.021 -7.218,13.507 -7.218,21.616l0,144l-252,0c-4.729,0 -9.246,0.914 -13.382,2.573l-0.017,0.007c0,-0 -0.071,0.029 -0.071,0.029c-13.205,5.339 -22.53,18.285 -22.53,33.391l0,216l-36,0c-19.869,0 -36,16.131 -36,36c0,19.869 16.131,36 36,36l36,0l0,180c0,12.41 6.293,23.362 15.859,29.836l0.019,0.012c-0,0 0.056,0.038 0.056,0.038c5.734,3.86 12.639,6.114 20.066,6.114l720,0c19.869,0 36,-16.131 36,-36c0,-19.869 -16.131,-36 -36,-36l-684,0l0,-396l216,0l0,108c0,8.109 2.687,15.595 7.218,21.616c6.571,8.733 17.022,14.384 28.782,14.384l432,0c19.869,0 36,-16.131 36,-36c0,-19.869 -16.131,-36 -36,-36l-396,0l0,-252l396,0c19.869,0 36,-16.131 36,-36c0,-19.869 -16.131,-36 -36,-36l-432,0Z" />{" "}
  </svg>
);

const RadialSVG = () => (
  <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
    <path d="M480.996,667.912l257.552,257.551c14.049,14.049 36.862,14.049 50.911,-0c14.05,-14.05 14.05,-36.862 0,-50.912l-277.463,-277.463l0,-124.183l184.905,-184.905l139.095,0c19.869,0 36,-16.131 36,-36c0,-19.869 -16.131,-36 -36,-36l-108,0l0,-108c0,-19.869 -16.131,-36 -36,-36c-19.868,0 -36,16.131 -36,36l0,119.081l-185,185l-257.544,-257.544c-14.049,-14.049 -36.862,-14.049 -50.911,0c-14.05,14.05 -14.05,36.862 -0,50.912l277.455,277.456l0,124.183l-195.455,195.456c-14.05,14.05 -14.05,36.862 -0,50.912c14.049,14.049 36.862,14.049 50.911,-0l185.544,-185.544Z" />
  </svg>
);

const DiagonalSVG = () => (
  <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
    <path d="M194.973,512c-0.292,-11.525 4.94,-22.988 15.004,-30.206l562.086,-403.098c16.146,-11.579 38.655,-7.871 50.234,8.275c11.58,16.146 7.872,38.656 -8.274,50.235l-522.619,374.794l126.67,90.841l352.71,-252.946c16.146,-11.579 38.656,-7.871 50.235,8.275c11.579,16.146 7.871,38.656 -8.275,50.235l-332.897,238.736l130.766,93.779l159.527,-114.404c16.146,-11.579 38.655,-7.872 50.235,8.275c11.579,16.146 7.871,38.655 -8.275,50.234l-139.714,100.195l141.637,101.574c16.146,11.579 19.854,34.089 8.274,50.235c-11.579,16.146 -34.088,19.854 -50.234,8.275l-562.086,-403.098c-10.064,-7.218 -15.296,-18.681 -15.004,-30.206Z" />
  </svg>
);

const CircularSVG = () => (
  <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
    <path d="M146.792,447.612l141.297,46.249c3.305,116.334 98.784,209.762 215.911,209.762c67.215,0 129.029,-31.103 169.226,-81.748l149.106,53.672c-20.556,38.143 -47.974,72.624 -81.279,101.531c-13.382,11.649 -15.647,31.591 -5.218,45.945c0.336,0.534 0.707,1.044 1.078,1.555c5.655,7.783 14.32,12.831 23.88,13.912c9.559,1.081 19.133,-1.905 26.383,-8.229c94.066,-81.641 148.824,-200.495 148.824,-326.261c0,-17.782 -1.098,-35.535 -3.283,-53.158c-1.199,-9.492 -6.32,-18.048 -14.118,-23.59c-7.799,-5.543 -17.563,-7.565 -26.922,-5.576c-0.668,0.063 -1.319,0.201 -1.97,0.339c-17.416,3.703 -29.098,20.125 -26.883,37.793c1.882,14.645 2.789,29.407 2.789,44.192c-0,36.011 -5.363,71.345 -15.576,104.997l-141.807,-51.045l0.002,-0.006l-0.456,-0.157l-43.863,-15.789l-0.219,0.61l-38.538,-13.27c-17.33,50.33 -63.89,84.555 -116.794,86.346c0,0 37.256,-108.083 56.468,-163.817c6.169,-17.897 -3.339,-37.407 -21.236,-43.576c-1.085,-0.374 -2.172,-0.749 -3.258,-1.123c-17.897,-6.169 -37.407,3.339 -43.576,21.236c-19.653,57.015 -58.354,169.291 -58.354,169.291c-37.449,-22.382 -62.543,-63.32 -62.543,-110.074c-0,-9.34 1.021,-18.639 3.038,-27.734l3.566,-10.895l-0.582,-0.191c0.082,-0.26 0.165,-0.518 0.249,-0.776l-83.562,-27.152l-0.192,0.596l-133.295,-43.63c34.229,-91.898 105.064,-167.554 197.868,-206.821c16.332,-6.957 24.648,-25.234 19.162,-42.118c-0.186,-0.599 -0.381,-1.197 -0.575,-1.795c-2.971,-9.144 -9.646,-16.62 -18.396,-20.604c-8.751,-3.984 -18.772,-4.11 -27.62,-0.347c-158.613,67.132 -263.524,223.223 -263.524,397.844c0,107.315 39.919,210.313 111.215,289.348c6.429,7.144 15.563,11.26 25.173,11.345c9.611,0.085 18.816,-3.869 25.371,-10.898c0.421,-0.449 0.84,-0.898 1.259,-1.347c12.117,-12.995 12.296,-33.093 0.414,-46.302c-59.649,-66.149 -93.045,-152.342 -93.045,-242.146c0,-19.113 1.502,-37.96 4.405,-56.388Z" />{" "}
  </svg>
);

const HierarchicalSVG = () => (
  <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
    <path d="M872,476c0,-11.76 -5.651,-22.211 -14.384,-28.782c-6.021,-4.531 -13.507,-7.218 -21.616,-7.218l-144,-0l0,-252c0,-4.729 -0.914,-9.246 -2.573,-13.382l-0.007,-0.017c0,0 -0.029,-0.071 -0.029,-0.071c-5.339,-13.205 -18.285,-22.53 -33.391,-22.53l-216,-0l-0,-36c-0,-19.869 -16.131,-36 -36,-36c-19.869,-0 -36,16.131 -36,36l-0,36l-180,0c-12.41,0 -23.362,6.293 -29.836,15.859l-0.012,0.019c-0,-0 -0.038,0.056 -0.038,0.056c-3.86,5.734 -6.114,12.639 -6.114,20.066l-0,720c-0,19.869 16.131,36 36,36c19.869,0 36,-16.131 36,-36l-0,-684l396,-0l-0,216l-108,0c-8.109,0 -15.595,2.687 -21.616,7.218c-8.733,6.571 -14.384,17.022 -14.384,28.782l-0,432c-0,19.869 16.131,36 36,36c19.869,0 36,-16.131 36,-36l-0,-396l252,-0l-0,396c-0,19.869 16.131,36 36,36c19.869,0 36,-16.131 36,-36l-0,-432Z" />
  </svg>
);

const RectangleIcon = (props: Partial<CustomIconComponentProps>) => (
  <Icon component={RectangleSVG} {...props} />
);

const RadialIcon = (props: Partial<CustomIconComponentProps>) => (
  <Icon component={RadialSVG} {...props} />
);

const DiagonalIcon = (props: Partial<CustomIconComponentProps>) => (
  <Icon component={DiagonalSVG} {...props} />
);

const CircularIcon = (props: Partial<CustomIconComponentProps>) => (
  <Icon component={CircularSVG} {...props} />
);

const HierarchicalIcon = (props: Partial<CustomIconComponentProps>) => (
  <Icon component={HierarchicalSVG} {...props} />
);

export default function PhylocanvasShapeDropDown() {
  const dispatch = useDispatch();
  const [options, setOptions] = React.useState<JSX.Element[]>([]);
  const {
    treeProps: { type },
  } = useSelector((state) => state.tree);

  console.log({ type });

  const types = useMemo(
    () => ({
      [TreeTypes.Rectangular]: {
        icon: <RectangleIcon />,
        title: i18n("PhylocanvasShapeDropDown.rectangular"),
      },
      [TreeTypes.Radial]: {
        icon: <RadialIcon />,
        title: i18n("PhylocanvasShapeDropDown.radial"),
      },
      [TreeTypes.Diagonal]: {
        icon: <DiagonalIcon />,
        title: i18n("PhylocanvasShapeDropDown.diagonal"),
      },
      [TreeTypes.Circular]: {
        icon: <CircularIcon />,
        title: i18n("PhylocanvasShapeDropDown.circular"),
      },
      [TreeTypes.Hierarchical]: {
        icon: <HierarchicalIcon />,
        title: i18n("PhylocanvasShapeDropDown.hierarchical"),
      },
    }),
    []
  );

  React.useEffect(() => {
    const current = Object.keys(types).map((key) => (
      <Menu.Item
        key={key}
        disabled={key === type}
        style={{ backgroundColor: "transparent" }}
        icon={types[key].icon}
      >
        {types[key].title}
      </Menu.Item>
    ));
    setOptions(current);
  }, [dispatch, type, types]);

  const overlay = (
    <Menu onClick={(item) => dispatch(updateTreeType({ treeType: item.key }))}>
      {options}
    </Menu>
  );

  return (
    <Dropdown overlay={overlay} trigger="click">
      <Button
        title={types[type].title}
        style={{ backgroundColor: `var(--grey-1)` }}
        key="changer"
        shape="circle"
        icon={types[type].icon}
      />
    </Dropdown>
  );
}
